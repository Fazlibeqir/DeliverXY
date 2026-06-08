#!/usr/bin/env python3
"""Check DeliverXY local observability endpoints."""

from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.request
from dataclasses import dataclass
from typing import Any


@dataclass
class CheckResult:
    name: str
    ok: bool
    detail: str


def fetch_url(url: str, timeout_seconds: int = 5) -> tuple[int | None, str]:
    try:
        with urllib.request.urlopen(url, timeout=timeout_seconds) as response:
            body = response.read().decode("utf-8", errors="replace")
            return response.status, body
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        return exc.code, body
    except Exception as exc:
        return None, str(exc)


def check_backend_health(base_url: str) -> CheckResult:
    status, body = fetch_url(f"{base_url}/actuator/health")
    if status != 200:
        return CheckResult("backend health", False, f"status={status}, body={body[:200]}")

    try:
        data = json.loads(body)
    except json.JSONDecodeError:
        return CheckResult("backend health", False, "response was not JSON")

    health_status = data.get("status")
    return CheckResult("backend health", health_status == "UP", f"status={health_status}")


def check_backend_prometheus(base_url: str) -> CheckResult:
    status, body = fetch_url(f"{base_url}/actuator/prometheus")
    if status != 200:
        return CheckResult("backend prometheus endpoint", False, f"status={status}, body={body[:200]}")

    has_metrics = "jvm_" in body or "http_server_requests" in body or "process_" in body
    return CheckResult(
        "backend prometheus endpoint",
        has_metrics,
        "metrics found" if has_metrics else "endpoint responded but expected metrics were not found",
    )


def check_prometheus(prometheus_url: str) -> CheckResult:
    status, body = fetch_url(f"{prometheus_url}/api/v1/targets")
    if status != 200:
        return CheckResult("prometheus targets", False, f"status={status}, body={body[:200]}")

    try:
        data: dict[str, Any] = json.loads(body)
    except json.JSONDecodeError:
        return CheckResult("prometheus targets", False, "response was not JSON")

    active_targets = data.get("data", {}).get("activeTargets", [])
    backend_targets = [
        target
        for target in active_targets
        if target.get("labels", {}).get("job") == "deliverxy-backend"
    ]

    if not backend_targets:
        return CheckResult("prometheus targets", False, "deliverxy-backend target not found")

    unhealthy = [target for target in backend_targets if target.get("health") != "up"]
    if unhealthy:
        return CheckResult("prometheus targets", False, f"backend target unhealthy: {unhealthy}")

    return CheckResult("prometheus targets", True, "deliverxy-backend target is up")


def check_grafana(grafana_url: str) -> CheckResult:
    status, body = fetch_url(f"{grafana_url}/api/health")
    if status != 200:
        return CheckResult("grafana health", False, f"status={status}, body={body[:200]}")

    try:
        data = json.loads(body)
    except json.JSONDecodeError:
        return CheckResult("grafana health", False, "response was not JSON")

    database_status = data.get("database")
    return CheckResult("grafana health", database_status == "ok", f"database={database_status}")


def print_result(result: CheckResult) -> None:
    marker = "PASS" if result.ok else "FAIL"
    print(f"[{marker}] {result.name}: {result.detail}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Check DeliverXY observability stack.")
    parser.add_argument("--base-url", default="http://localhost:8080")
    parser.add_argument("--prometheus-url", default="http://localhost:9090")
    parser.add_argument("--grafana-url", default="http://localhost:3001")
    args = parser.parse_args()

    checks = [
        check_backend_health(args.base_url),
        check_backend_prometheus(args.base_url),
        check_prometheus(args.prometheus_url),
        check_grafana(args.grafana_url),
    ]

    for check in checks:
        print_result(check)

    if not all(check.ok for check in checks):
        sys.exit(1)

    print("\nObservability stack is healthy.")


if __name__ == "__main__":
    main()
