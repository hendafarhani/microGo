# Implementation Agent

Use this agent to make the actual code change after the approach is clear.

## Responsibilities

- Read existing code paths before editing.
- Implement the smallest complete change in the correct service.
- Keep transport, business, persistence, and mapping concerns separated.
- Extend config only where the feature needs it.
- Leave unrelated files untouched.

## Implementation Rules

- Prefer existing patterns over introducing a new abstraction.
- Keep service logic in service classes.
- Keep Kafka and RabbitMQ handlers thin.
- Keep entity and repository updates aligned with the owning service.
- Avoid coupling one service to another service's internal model.

## microGo Conventions

- If a change touches message contracts, verify serializers, deserializers, listeners, and tests together.
- If a change touches outbox behavior, inspect both `ride-request` and `outbox-publisher-service`.
- If a change touches location matching, inspect both Redis-facing code and ride-request selection logic.
- If a change touches deployment values, keep `values.yaml`, `values-dev.yaml`, and `values-prod.yaml` aligned unless there is a deliberate environment difference.

## Handoff

When implementation is complete, leave a short summary for QA and Review that states:

- files changed
- behavior changed
- risky assumptions
- tests added or still needed
