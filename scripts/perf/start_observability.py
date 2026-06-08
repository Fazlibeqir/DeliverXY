#!/usr/bin/env python3
"""Start DeliverXY with Prometheus and Grafana."""

from __future__ import annotations

from perf_common import docker_compose, require_success


def main() -> None:
    exit_code = docker_compose(["up", "-d"])
    require_success(exit_code, "Failed to start DeliverXY observability stack.")

    print("\nDeliverXY observability stack started.")
    print("Backend:    http://localhost:8080")
    print("Admin:      http://localhost:3000")
    print("Prometheus: http://localhost:9090")
    print("Grafana:    http://localhost:3001")
    print("Grafana login: admin / admin")


if __name__ == "__main__":
    main()
