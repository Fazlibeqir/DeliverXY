#!/usr/bin/env python3
"""Stop DeliverXY observability stack."""

from __future__ import annotations

import argparse

from perf_common import docker_compose, require_success


def main() -> None:
    parser = argparse.ArgumentParser(description="Stop DeliverXY observability stack.")
    parser.add_argument(
        "--volumes",
        action="store_true",
        help="Remove Docker volumes too. This deletes local PostgreSQL, Prometheus, and Grafana data.",
    )
    args = parser.parse_args()

    command = ["down"]
    if args.volumes:
        command.append("-v")

    exit_code = docker_compose(command)
    require_success(exit_code, "Failed to stop DeliverXY observability stack.")

    print("\nDeliverXY observability stack stopped.")
    if args.volumes:
        print("Docker volumes were removed.")


if __name__ == "__main__":
    main()
