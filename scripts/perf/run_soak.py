#!/usr/bin/env python3
"""Run DeliverXY long-running k6 stability test."""

from __future__ import annotations

import argparse

from perf_common import add_common_k6_args, require_success, run_k6_script


def main() -> None:
    parser = argparse.ArgumentParser(description="Run DeliverXY long-running k6 stability test.")
    add_common_k6_args(parser)
    args = parser.parse_args()

    exit_code = run_k6_script("soak", args)
    require_success(exit_code, "Long-running stability test failed.")


if __name__ == "__main__":
    main()
