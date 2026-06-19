# Current Focus Memory

## Active Direction

The near-term shape of the project appears to be:

- strengthen message-driven ride flow behavior
- keep the dashboard WebSocket projection aligned with outbox-driven ride events
- keep local Docker Compose testing reliable
- continue Kubernetes and Terraform readiness
- maintain clear documentation for future iteration

## Immediate Working Assumptions

- Cross-service changes should start with architecture review.
- Event contract changes should trigger implementation, QA, and review work across all impacted services.
- Deployment-related changes should update both runtime config and operational docs when needed.

## Open Risks To Revisit

- Drift between local Docker Compose behavior and Helm or Terraform deployment assumptions
- Event contract changes spanning multiple services without synchronized tests
- Submodule or independently versioned service changes getting out of sync with root-repo docs
- Incomplete operational memory after infra changes

## How To Use This File

Refresh this memory after meaningful project progress. Replace stale priorities instead of appending a long changelog.
