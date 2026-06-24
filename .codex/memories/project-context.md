# Project Context Memory

## What microGo Is

`microGo` is a ride-sharing backend organized as multiple Spring Boot services plus deployment infrastructure. It combines synchronous platform pieces with asynchronous ride-processing flows.

## Main Services

- `ride-request`: ride lifecycle, matching, outbox creation, rider offer flow, RabbitMQ response handling
- `location-saver`: consumes and stores location data, including Redis-backed lookup support
- `driver-location-generator`: owns simulated live driver movement, Redis GEO driver state, and Kafka driver movement events
- `simulation-service`: owns scenario execution, synthetic passenger and driver profiles, simulated ride demand, driver accept or refuse decisions, forecasting metrics, and optimization-facing simulation results
- `optimization-service`: planned optimization consumer that builds live snapshots from simulation output plus driver geo-state, runs Timefold-based driver repositioning and comparison logic, stores optimization history in PostgreSQL, and publishes recommendations without replacing `ride-request` dispatch ownership in phase one
- `driver-location-streamer`: streams driver or rider location updates over Kafka and WebSocket-related paths
- `dashboard-service`: streams ride request dashboard updates from Kafka-backed outbox events to WebSocket clients
- `outbox-publisher-service`: publishes pending outbox events and tracks acknowledgements
- `gateway`: Spring Cloud Gateway entrypoint
- `discovery`: Eureka service discovery
- `centralized-config`: Spring Cloud Config server

## Infra Surfaces

- `docker-compose.yml`: local full-stack orchestration
- `helm/`: service deployment charts plus shared templates
- `terraform/`: environment and cluster provisioning modules
- `.github/workflows/`: CI for build, image push, and analysis

## Architectural Shape

- Business events move primarily through Kafka.
- Some user or rider response flows use RabbitMQ.
- Redis supports fast location-based lookups.
- Driver movement simulation should treat Redis GEO plus `DriverLocationUpdatedEvent` as the live driver source of truth.
- `driver-location-generator` consumes ride-decision events and translates them to movement (added 2026-06-24): `simulation.driver.accepted` → drive to pickup (`activeRideId` set); `simulation.driver.refused` → stay available (no-op); `ride.cancelled` → if it matches the driver's `activeRideId`, halt and reset to `IDLE`+available and emit `driver.available`. The trip still begins on the explicit `driver.command.start-trip` (guarded so it only applies when its `rideId` matches the driver's `activeRideId`). `DriverLocationUpdatedEvent` now also carries `status` + `activeRideId` so consumers see busy/free directly. Producers of `ride.cancelled` and `start-trip` are not yet built, so the cancel/trip paths are dormant until a producer exists.
- `simulation-service` should publish `DriverGeneratedEvent` and consume `DriverLocationUpdatedEvent` rather than moving drivers directly.
- `ride-request` consumes `DriverGeneratedEvent` and maintains an idempotent MySQL `driver` dispatch projection keyed by `driverId`. MySQL table names are lowercase because local Linux containers use case-sensitive table names.
- Dispatch matching is availability-aware (2026-06-24): availability ownership lives in `simulation-service` (NOT the generator). `simulation-service` maintains a Redis SET `available_drivers` of driver IDs (no positions) via `DriverAvailabilityRegistry`: SADD on driver-generated, SREM on accept, SADD on cancel and on driver-reached-destination. `ride-request` runs its nearest-5 GEORADIUS on `vehicle_location` (positions, generator-owned) and intersects with the `available_drivers` SET. `vehicle_location` holds ALL drivers (generator + location-saver) for `optimization-service`'s full-fleet view. The generator does NOT maintain any availability index — it only owns movement/positions. Rationale: positions belong in one place (`vehicle_location`); availability is a decision/lifecycle concern owned by simulation.
- Decision (2026-06-24): live driver status/availability/position is deliberately NOT persisted to Postgres. The `driver_location` DB holds only `driver_profile` (stable identity); live state stays in Redis (`driver:geo-state:*`, `vehicle_location`, `available_drivers`) and the `DriverLocationUpdatedEvent`. Do not add a status column to `driver_profile` (per-tick write amplification). If durability/history is ever needed, add a separate transition-scoped table (`driver_live_state` upsert-on-transition, or append-only `driver_status_history`) — never on the 5s movement path.
- `optimization-service` should consume simulation output plus live driver state, compare nearest-five baseline against optimized repositioning, and publish recommendation events rather than direct movement commands.
- MySQL stores durable ride and user state.
- PostgreSQL is the planned durable store for simulation scenario config, run state, profiles, metrics, and results.
- PostgreSQL is also the planned durable store for optimization runs, solver results, baseline-vs-optimized metrics, and repositioning recommendations.
- Outbox-based publication is a first-class pattern in the repo.

## Repo Notes

- The root repo acts as a coordination layer across services and infra.
- Some directories are independently managed units or submodules.
- Existing local modifications were present in `ride-request` and `location-saver` when this memory was created.

## Documentation References

- `README.md`: project overview
- `LOCAL_TESTING.md`: repeatable local validation flow
- `docs/`: architecture and deployment notes
