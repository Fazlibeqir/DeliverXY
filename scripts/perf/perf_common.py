#!/usr/bin/env python3
"""Shared helpers for DeliverXY performance scripts."""

from __future__ import annotations

import argparse
import os
import platform
import subprocess
import sys
from pathlib import Path
from typing import Sequence

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_COMPOSE_FILES = ["docker-compose.yml", "docker-compose.observability.yml"]


def is_windows() -> bool:
    return platform.system().lower().startswith("win")


def is_docker_desktop_platform() -> bool:
    system = platform.system().lower()
    return system in {"darwin", "windows"}


def run_command(command: Sequence[str], cwd: Path | None = None, env: dict[str, str] | None = None) -> int:
    print("$ " + " ".join(command))
    completed = subprocess.run(
        list(command),
        cwd=str(cwd or REPO_ROOT),
        env=env,
        text=True,
    )
    return completed.returncode


def require_success(exit_code: int, message: str) -> None:
    if exit_code != 0:
        raise SystemExit(f"{message} Exit code: {exit_code}")


def docker_compose_base_command() -> list[str]:
    command = ["docker", "compose"]
    for compose_file in DEFAULT_COMPOSE_FILES:
        command.extend(["-f", compose_file])
    return command


def docker_compose(args: Sequence[str]) -> int:
    return run_command(docker_compose_base_command() + list(args))


def default_base_url() -> str:
    return os.environ.get("BASE_URL", "http://localhost:8080")


def docker_k6_base_url() -> str:
    if is_docker_desktop_platform():
        return os.environ.get("BASE_URL", "http://host.docker.internal:8080")
    return os.environ.get("BASE_URL", "http://localhost:8080")


def ensure_results_dir() -> Path:
    results_dir = REPO_ROOT / "performance-tests" / "results"
    results_dir.mkdir(parents=True, exist_ok=True)
    return results_dir


def add_common_k6_args(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        "--base-url",
        default=None,
        help="Backend URL. Defaults to BASE_URL env var or localhost.",
    )
    parser.add_argument(
        "--docker",
        action="store_true",
        help="Run k6 through Docker instead of local k6.",
    )
    parser.add_argument(
        "--test-user-email",
        default=os.environ.get("TEST_USER_EMAIL", "perf-local@deliverxy.test"),
        help="Email for the performance test user.",
    )
    parser.add_argument(
        "--test-user-password",
        default=os.environ.get("TEST_USER_PASSWORD", "Password123!"),
        help="Password for the performance test user.",
    )
    parser.add_argument(
        "--register-test-user",
        default=os.environ.get("REGISTER_TEST_USER", "true"),
        choices=["true", "false"],
        help="Whether k6 should register the test user during setup.",
    )
    parser.add_argument(
        "--auth-enabled",
        default=os.environ.get("AUTH_ENABLED", "true"),
        choices=["true", "false"],
        help="Whether k6 should use auth flow.",
    )
    parser.add_argument(
        "--summary-export",
        action="store_true",
        help="Export k6 JSON summary into performance-tests/results.",
    )


def build_k6_env(args: argparse.Namespace, for_docker: bool = False) -> dict[str, str]:
    env = os.environ.copy()
    base_url = args.base_url or (docker_k6_base_url() if for_docker else default_base_url())
    env.update(
        {
            "BASE_URL": base_url,
            "TEST_USER_EMAIL": args.test_user_email,
            "TEST_USER_IDENTIFIER": args.test_user_email,
            "TEST_USER_PASSWORD": args.test_user_password,
            "REGISTER_TEST_USER": args.register_test_user,
            "AUTH_ENABLED": args.auth_enabled,
        }
    )
    return env


def run_k6_script(script_name: str, args: argparse.Namespace) -> int:
    script_path = REPO_ROOT / "performance-tests" / "k6" / f"{script_name}.js"
    if not script_path.exists():
        raise SystemExit(f"k6 script not found: {script_path}")

    results_dir = ensure_results_dir()
    summary_path = results_dir / f"{script_name}-summary.json"

    if args.docker:
        env = build_k6_env(args, for_docker=True)
        command = ["docker", "run", "--rm", "-i"]

        if not is_docker_desktop_platform():
            command.extend(["--network", "host"])

        command.extend(
            [
                "-e",
                f"BASE_URL={env['BASE_URL']}",
                "-e",
                f"TEST_USER_EMAIL={env['TEST_USER_EMAIL']}",
                "-e",
                f"TEST_USER_IDENTIFIER={env['TEST_USER_IDENTIFIER']}",
                "-e",
                f"TEST_USER_PASSWORD={env['TEST_USER_PASSWORD']}",
                "-e",
                f"REGISTER_TEST_USER={env['REGISTER_TEST_USER']}",
                "-e",
                f"AUTH_ENABLED={env['AUTH_ENABLED']}",
                "-v",
                f"{REPO_ROOT / 'performance-tests'}:/performance-tests",
                "grafana/k6:latest",
                "run",
            ]
        )

        if args.summary_export:
            command.extend(["--summary-export", f"/performance-tests/results/{script_name}-summary.json"])

        command.append(f"/performance-tests/k6/{script_name}.js")
        return run_command(command)

    env = build_k6_env(args, for_docker=False)
    command = ["k6", "run"]

    if args.summary_export:
        command.extend(["--summary-export", str(summary_path)])

    command.append(str(script_path))
    return run_command(command, env=env)
