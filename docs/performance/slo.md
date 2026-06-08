# DeliverXY Performance SLOs

This document defines initial performance objectives for DeliverXY.

These are starting targets, not final production guarantees. First run the baseline tests, record real numbers, then tighten the objectives gradually.

## Scope

Covered components:

- Spring Boot REST API
- PostgreSQL-backed delivery workflows
- JWT authentication
- Admin and client delivery endpoints
- Docker Compose local environment
- Future Kubernetes/AWS deployment environments

## Key metrics

| Metric | Meaning |
|---|---|
| p95 latency | 95% of requests complete faster than this value |
| p99 latency | 99% of requests complete faster than this value |
| Error rate | Percentage of failed HTTP requests |
| Throughput | Requests per second handled by the system |
| Saturation | CPU, memory, database, or connection pool pressure |

## Initial API objectives

| Endpoint group | p95 target | p99 target | Error-rate target |
|---|---:|---:|---:|
| Health check | < 200ms | < 500ms | < 0.1% |
| Login/register | < 500ms | < 1200ms | < 1% |
| Current user | < 300ms | < 800ms | < 1% |
| User deliveries | < 700ms | < 1500ms | < 1% |
| Create delivery | < 900ms | < 2000ms | < 1% |
| Admin delivery list | < 1000ms | < 2500ms | < 1% |
| Admin dashboard | < 1200ms | < 3000ms | < 1% |

## Test categories

### Smoke test

Goal: prove the system is alive and the main authenticated journey works.

Target:

- 1 virtual user
- 1 iteration
- p95 below 1500ms
- error rate below 5%

### Load test

Goal: verify expected normal usage.

Target:

- 10 virtual users
- 3 minutes total
- p95 below 800ms
- p99 below 1500ms
- error rate below 2%

### Stress test

Goal: find where the system starts degrading.

Target:

- ramp up to 100 virtual users
- record first clear bottleneck
- p95 below 2500ms while still stable
- error rate below 10%

### Spike test

Goal: verify behavior during sudden traffic jumps.

Target:

- jump from 5 to 80 virtual users
- system should recover after spike
- p95 below 2000ms
- error rate below 8%

### Soak test

Goal: detect memory leaks, connection leaks, and slow degradation.

Target:

- 10 virtual users
- 15+ minutes locally
- longer in staging
- no continuous latency increase
- no continuous memory increase

## Release gate policy

A pull request should not be merged if the smoke test fails.

A deployment should be reviewed if:

- p95 latency increases by more than 30% from baseline
- error rate is above target
- backend logs show repeated 5xx errors
- database connection pool exhaustion appears
- PostgreSQL CPU or query latency spikes under normal load

## Baseline update policy

Update the baseline only when:

1. The test environment is documented.
2. The test data volume is documented.
3. The same test scenario was run at least 3 times.
4. Results are stable enough to compare.
5. Any optimization or regression is explained.

## Notes

Local Docker Compose numbers are not production numbers. They are useful for regressions and learning. For real capacity planning, repeat tests on the target deployment environment: EC2, ECS, EKS, RDS, and/or ElastiCache.
