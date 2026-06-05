# microGo Agent Roles

Use these roles when planning, implementing, testing, and reviewing changes in microGo.

## Architecture Agent

Responsible for architecture choices, service boundaries, and design patterns.

- Checks whether a requirement belongs in an existing service or needs a new service/module.
- Reviews event flow across Kafka, RabbitMQ, Redis, MySQL, Eureka, Gateway, and Config Server.
- Validates design patterns, transaction boundaries, retries, idempotency, and failure handling.
- Flags coupling, duplicated responsibilities, scalability risks, and deployment concerns.
- Produces architecture notes before implementation when a requirement affects multiple services.

## Developer Agent

Responsible for implementing new requirements.

- Reads the existing code style before writing code.
- Implements focused changes in the correct microservice.
- Keeps business logic in services, persistence in repositories/entities, and transport logic in Kafka/Rabbit/Web layers.
- Updates configuration, Docker, Helm, or Terraform only when the requirement needs it.
- Leaves unrelated code untouched.

## QA Agent

Responsible for generating and maintaining tests.

- Adds unit tests for service logic, mappers, schedulers, serializers, and listeners.
- Adds integration tests for Redis, MySQL, Kafka, and RabbitMQ flows when behavior crosses infrastructure boundaries.
- Adds container-based tests with Testcontainers when external dependencies are required.
- Verifies edge cases, invalid payloads, retries, empty results, duplicate messages, and failure paths.
- Keeps tests deterministic and close to the behavior being changed.

## Reviewer Agent

Responsible for code correction, enhancement, and refactoring.

- Reviews code for bugs, regressions, readability, naming, and maintainability.
- Refactors only when it improves clarity or removes real duplication.
- Checks that tests cover the changed behavior.
- Verifies that architecture decisions are followed in the implementation.
- Produces concise review feedback with severity, file references, and suggested fixes.

## Suggested Workflow

1. Architecture Agent validates the approach for cross-service or infrastructure-heavy changes.
2. Developer Agent implements the requirement.
3. QA Agent adds or updates tests.
4. Reviewer Agent reviews, refactors, and confirms readiness.
