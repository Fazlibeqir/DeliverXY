# JMeter Performance Tests

This folder is reserved for Apache JMeter test plans.

For the first implementation, k6 is the primary performance testing tool because it is easier to version, review, and run in CI. JMeter is still useful for GUI-based test design, enterprise teams, and compatibility with Taurus.

## Recommended JMeter plan

Create a file later named:

```txt
performance-tests/jmeter/deliverxy-api.jmx
```

The JMeter plan should include:

1. Thread Group: Client delivery journey
2. HTTP Request Defaults: `http://localhost:8080`
3. HTTP Header Manager: `Content-Type: application/json`
4. Register request: `POST /api/auth/register`
5. Login request: `POST /api/auth/login`
6. JSON Extractor: extract `data.accessToken`
7. Authenticated request: `GET /api/auth/me`
8. Delivery list request: `GET /api/deliveries/mine`
9. Create delivery request: `POST /api/deliveries`
10. Summary Report listener
11. View Results Tree listener only for debugging, not real load tests

## Suggested load profiles

| Profile | Threads | Ramp-up | Duration | Goal |
|---|---:|---:|---:|---|
| Smoke | 1 | 1s | 1 iteration | Verify setup |
| Load | 10 | 30s | 3m | Expected local traffic |
| Stress | 50-100 | 2m | 5m | Find bottleneck |
| Soak | 10 | 1m | 30m+ | Stability and leaks |

## CLI execution

Run JMeter in non-GUI mode:

```bash
jmeter -n \
  -t performance-tests/jmeter/deliverxy-api.jmx \
  -l performance-tests/results/jmeter-results.jtl \
  -e \
  -o performance-tests/results/jmeter-dashboard
```

## Taurus execution

After the JMX file exists, Taurus can run it like this:

```yaml
execution:
  - executor: jmeter
    scenario: deliverxy-api

scenarios:
  deliverxy-api:
    script: ../jmeter/deliverxy-api.jmx
```

## Important JMeter rules

- Do not use View Results Tree in serious load tests.
- Always run CLI mode for real tests.
- Use realistic ramp-up; instant high traffic can test spike behavior, but it is not normal load behavior.
- Keep assertions strict enough to detect real failures.
- Export results and compare them with the k6 baseline.

## Current status

JMeter plan file is not committed yet. Add it after the k6 tests are verified against the running backend.
