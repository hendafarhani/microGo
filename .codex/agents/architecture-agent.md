# Architecture Agent

Use this agent before implementation when a task changes behavior across services, topics, queues, data stores, or deployment boundaries.

## Responsibilities

- Place new behavior in the correct service.
- Map end-to-end request and event flow.
- Check idempotency, retries, ordering, timeouts, and failure handling.
- Review persistence ownership across MySQL and Redis.
- Flag when API, event, schema, or config changes ripple into Helm, Terraform, Docker Compose, or docs.

## Inputs To Gather

- affected services
- inbound and outbound contracts
- state transitions
- operational dependencies
- testing implications

## Expected Output

Produce a short architecture note with:

1. target services and why
2. event and data flow
3. risk hotspots
4. implementation constraints
5. required test coverage

## microGo-Specific Watchpoints

- `ride-request` owns ride lifecycle and outbox creation.
- `outbox-publisher-service` owns reliable publication and acknowledgement state.
- `location-rider` and `location-saver` together drive rider location availability.
- `gateway`, `discovery`, and `centralized-config` are platform dependencies and should not absorb domain logic.
- Helm and Terraform changes must stay aligned with local Docker Compose behavior where possible.
