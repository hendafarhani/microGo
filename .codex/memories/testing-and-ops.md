# Testing And Ops Memory

## Local Validation Baseline

Use `LOCAL_TESTING.md` as the main runbook for local end-to-end validation.

## Known Local Stack Shape

- Docker Compose orchestrates the local environment.
- Core checks include gateway reachability, config endpoint reachability, Kafka topic behavior, Redis data presence, and MySQL persistence.
- The outbox flow is important enough to verify explicitly, including dashboard WebSocket delivery and acknowledgement handling.

## Testing Heuristics

- Prefer unit tests for internal logic changes.
- Add integration tests when a change crosses Kafka, RabbitMQ, Redis, or MySQL boundaries.
- When event envelopes or status enums change, verify serialization and persistence together.

## Operational Heuristics

- If a change alters startup config, inspect Spring properties, centralized config, and deployment values together.
- If a change alters networking or routing, inspect gateway, discovery, Helm, and Terraform impact together.
- If a change alters local workflows, update `LOCAL_TESTING.md`.
