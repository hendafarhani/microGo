# Current Focus Memory

## Active Direction

The near-term shape of the project appears to be:

- strengthen message-driven ride flow behavior
- establish a dedicated live driver movement source for simulation and dispatch testing
- establish a dedicated simulation forecasting service that creates world state and feeds optimization consumers
- establish a dedicated optimization service that improves simulated future state with driver repositioning recommendations before any dispatch replacement
- keep the dashboard WebSocket projection aligned with outbox-driven ride events
- align live movement and ride-event contracts for a gateway-served Angular operations map
- keep local Docker Compose testing reliable
- continue Kubernetes and Terraform readiness
- maintain clear documentation for future iteration

## Immediate Working Assumptions

- Cross-service changes should start with architecture review.
- Event contract changes should trigger implementation, QA, and review work across all impacted services.
- Simulation and movement changes should preserve the boundary where `driver-location-generator` moves drivers and `simulation-service` decides scenario behavior.
- Optimization changes should preserve the boundary where `simulation-service` predicts, `optimization-service` improves, and `driver-location-generator` executes movement recommendations.
- Deployment-related changes should update both runtime config and operational docs when needed.

## Open Risks To Revisit

- Drift between local Docker Compose behavior and Helm or Terraform deployment assumptions
- Event contract changes spanning multiple services without synchronized tests
- Identifier drift between live movement events and ride acceptance events
- Driver projection drift between `DriverGeneratedEvent`, Redis GEO membership, and MySQL `driver`
- Drift between simulation event contracts and optimization-consumer expectations
- Drift between optimization recommendation contracts and the future movement-command handoff
- Zone taxonomy drift across simulation, optimization, and live movement services
- Submodule or independently versioned service changes getting out of sync with root-repo docs
- Incomplete operational memory after infra changes

## How To Use This File

Refresh this memory after meaningful project progress. Replace stale priorities instead of appending a long changelog.
