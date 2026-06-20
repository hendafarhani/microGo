# Testing And Ops Memory

## Local Validation Baseline

Use `LOCAL_TESTING.md` as the main runbook for local end-to-end validation.

## Known Local Stack Shape

- Docker Compose orchestrates the local environment.
- PostgreSQL initialization reads the `POSTGRES_DATABASES` Docker Compose value, creates dedicated `driver_location`, `simulation_service`, and `optimization_service` databases, and now provisions each service table set from `postgres-init/01_create_service_databases.sh`; Hibernate DDL auto-creation is disabled for those services. Existing PostgreSQL data directories require the initializer to be run manually when a database or table set is added.
- MySQL initialization provisions the shared `ride_requests_db` schema from `mysql-init/02_schema.sql`; if an existing volume predates schema changes, rerun that script manually before restarting `ride-request`, `outbox-publisher-service`, and `dashboard-service`.
- `simulation-service` and `optimization-service` are built and started by the root Docker Compose stack with Config Server, Eureka, Kafka, Redis, and PostgreSQL dependencies.
- Core checks include gateway reachability, config endpoint reachability, Kafka topic behavior, Redis data presence, and MySQL persistence.
- The outbox flow is important enough to verify explicitly, including dashboard WebSocket delivery and acknowledgement handling.

## Testing Heuristics

- Prefer unit tests for internal logic changes.
- Add integration tests when a change crosses Kafka, RabbitMQ, Redis, or MySQL boundaries.
- When event envelopes or status enums change, verify serialization and persistence together.
- For optimization work, add unit tests for snapshot assembly, constraint scoring, and baseline-vs-optimized comparison before adding solver-heavy integration coverage.
- `optimization-service` follows the repository's service-contract convention: injectable service APIs live under `service` subpackages and Spring beans live under `service/serviceimpl` with `Impl` suffixes.
- Keep builders out of `optimization-service` service implementations. Use focused mapper classes for driver snapshots, ride snapshots, simulation state, optimization snapshots, comparison metrics, distance matrices, Timefold planning models, and persisted or published results.
- For optimization integration work, verify Kafka contract handling, Redis GEO reads, and PostgreSQL result persistence together because stale snapshot bugs can hide between those boundaries.
- On the current local JDK 25 setup, JaCoCo 0.8.12 and Mockito inline mocking can fail before assertions run. When validating targeted unit tests locally, prefer `-Djacoco.skip=true` and keep `mock-maker-subclass` test overrides in mind.

## Operational Heuristics

- If a change alters startup config, inspect Spring properties, centralized config, and deployment values together.
- MySQL table names are canonicalized to lowercase. The driver dispatch projection is `driver`.
- `simulation-service` now follows the repo's `service` plus `serviceimpl` split and reads runtime properties from `centralized-config/centralized-configuration/simulation-service.properties`; when debugging bean wiring or missing config, check the interface injection point and that config file together.
- In `simulation-service`, entity state transitions such as run completion and metrics status updates should be routed through mapper helpers rather than applied inline in services; check `mapper/` first when a status mutation looks wrong.
- If a change alters networking or routing, inspect gateway, discovery, Helm, and Terraform impact together.
- If a change alters local workflows, update `LOCAL_TESTING.md`.
- If optimization recommendations begin driving actual movement commands, inspect `driver-location-generator`, centralized config, and local replay steps together before treating the flow as operationally complete.
- `dashboard-service` still runs with `spring.jpa.hibernate.ddl-auto=none`, but now relies on MySQL-specific startup SQL init for cross-service read-model columns. Keep `centralized-config/centralized-configuration/dashboard-service.properties` and `dashboard-service/src/main/resources/schema-mysql.sql` aligned whenever dashboard queries add new columns.
