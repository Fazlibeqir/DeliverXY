#!/usr/bin/env python3
"""Run DeliverXY k6 spike test."""

from __future__ import annotations

import argparse

from perf_common import add_common_k6_args, require_success, run_k6_script


def main() -> None:
    parser = argparse.ArgumentParser(description="Run DeliverXY k6 spike test.")
    add_common_k6_args(parser)
    args = parser.parse_args()

    exit_code = run_k6_script("spike", args)
    require_success(exit_code, "Spike test failed.")


if __name__ == "__main__":
    main()
