# DeliverXY Bottleneck Analysis

## 1. Incident or test context

| Field | Value |
|---|---|
| Date | TODO |
| Analyst | TODO |
| Branch | TODO |
| Commit SHA | TODO |
| Test type | TODO |
| Environment | TODO |
| Related baseline report | TODO |

## 2. Problem statement

Describe the observed performance problem clearly.

Example:

> During the load test, `/api/deliveries/mine` exceeded the p95 latency target after 10 virtual users. Latency increased from 450ms to 1800ms while error rate remained below 1%.

## 3. Impact

| Area | Impact |
|---|---|
| Users affected | TODO |
| Endpoint or workflow | TODO |
| Latency impact | TODO |
| Error-rate impact | TODO |
| Business impact | TODO |

## 4. Evidence

Add screenshots, k6 summaries, logs, database observations, and metrics.

| Evidence | Finding |
|---|---|
| k6 p95 latency | TODO |
| k6 p99 latency | TODO |
| Backend logs | TODO |
| PostgreSQL slow query log | TODO |
| CPU usage | TODO |
| Memory usage | TODO |
| Connection pool metrics | TODO |

## 5. Suspected bottleneck category

Check all that apply:

- [ ] Application code
- [ ] Database query
- [ ] Missing database index
- [ ] N+1 query pattern
- [ ] Serialization or large payload
- [ ] Authentication/JWT overhead
- [ ] Connection pool exhaustion
- [ ] Thread pool saturation
- [ ] File upload or storage I/O
- [ ] External API call
- [ ] Network or infrastructure
- [ ] Frontend/admin dashboard query pattern

## 6. Root-cause analysis

### What changed?

TODO

### Why did latency increase?

TODO

### Why did the system not absorb the load?

TODO

### Why was this not caught earlier?

TODO

## 7. Validation steps

| Step | Command or method | Result |
|---:|---|---|
| 1 | Re-run baseline test | TODO |
| 2 | Check backend logs | TODO |
| 3 | Check slow queries | TODO |
| 4 | Inspect endpoint code path | TODO |
| 5 | Apply temporary fix | TODO |
| 6 | Re-run same test | TODO |

## 8. Fix candidates

| Priority | Fix | Expected impact | Risk | Owner |
|---|---|---|---|---|
| High | TODO | TODO | Low/Medium/High | TODO |
| Medium | TODO | TODO | Low/Medium/High | TODO |
| Low | TODO | TODO | Low/Medium/High | TODO |

## 9. Selected fix

Explain which fix was selected and why.

TODO

## 10. Before and after results

| Metric | Before | After | Change |
|---|---:|---:|---:|
| p95 latency | TODO | TODO | TODO |
| p99 latency | TODO | TODO | TODO |
| Error rate | TODO | TODO | TODO |
| Requests per second | TODO | TODO | TODO |
| CPU usage | TODO | TODO | TODO |
| Memory usage | TODO | TODO | TODO |
| Database query time | TODO | TODO | TODO |

## 11. Regression prevention

Add concrete prevention steps.

- [ ] Add or update k6 threshold
- [ ] Add CI performance smoke gate
- [ ] Add database index migration
- [ ] Add pagination or limit
- [ ] Add cache where appropriate
- [ ] Add observability dashboard panel
- [ ] Add alert for p95 latency
- [ ] Document endpoint performance expectation

## 12. Final conclusion

Write a direct conclusion.

Example:

> The bottleneck was caused by an unpaginated delivery lookup and missing index on client/status columns. After adding pagination and indexing, p95 latency dropped from 1800ms to 420ms under the same load profile.
