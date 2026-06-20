# Simulation Service

## Architecture Proposal

`simulation-service` should be a dedicated Spring Boot microservice that owns scenario execution, synthetic passenger and driver population generation, simulated rider decisioning, and forecast materialization for downstream optimization. It should not move drivers directly. Instead, it publishes `DriverGeneratedEvent`, consumes live `DriverLocationUpdatedEvent`, and treats Redis GEO plus the driver location stream as the authoritative movement view.

### Target Services And Why

- `simulation-service`
  - owns simulation runs, scenario loading, passenger creation, driver profiles, demand generation, acceptance and refusal decisions, metric collection, and optimization-ready forecasts
- `driver-location-generator`
  - owns physical driver placement and movement after receiving `DriverGeneratedEvent`
- `ride-request`
  - owns nearest-driver lookup and downstream ride lifecycle transitions
- `optimization-service`
  - consumes the final contract from `simulation-service` and improves predicted outcomes without owning movement execution

### Event And Data Flow

1. `SimulationController` starts a simulation run.
2. `ScenarioEngine` loads `scenario_config`, creates `simulation_run`, and publishes `ScenarioStartedEvent`.
3. `PopulationEngine` generates passengers and drivers.
4. Passengers are inserted into `passenger_profile` immediately when created.
5. Drivers are inserted into `driver_profile` and `DriverGeneratedEvent` is published.
6. `driver-location-generator` places drivers, updates Redis GEO `vehicle_location`, and emits `DriverLocationUpdatedEvent`.
7. `DriverLocationConsumer` maintains the simulation view of live driver positions.
8. `RideRequestGenerator` creates requests based on scenario conditions and `RideRequestPublisher` emits `SimulatedRideRequestedEvent`.
9. `ride-request` notifies candidate drivers and emits `DriverNotifiedEvent`.
10. `RideResponseConsumer` and `DriverDecisionEngine` decide acceptance or refusal using pickup distance, fatigue, driver preferences, scenario pressure, traffic, fare expectation, and destination preference.
11. `simulation-service` publishes `DriverAcceptedEvent` or `DriverRefusedEvent`.
12. It consumes `RideAssignedEvent`, `RideCancelledEvent`, `DriverReachedPickupEvent`, and `DriverReachedDestinationEvent` to maintain ride progression metrics.
13. `SimulationMetricsCollector` updates `simulation_metrics` and publishes `SimulationMetricsUpdatedEvent`.
14. `ForecastCalculator` and `SimulationResultBuilder` persist `simulation_result` and publish `SimulationCompletedEvent`.

### Risk Hotspots

- Per-driver event ordering should stay keyed by `driverId`.
- Passenger creation must be persisted before any request publication so replay does not invent orphan ride requests.
- `ride-request` and `simulation-service` need aligned IDs for passenger, ride, and driver references.
- Redis remains a read-side live geo-source, not a durable ownership store for simulation history.
- The optimization contract should be versioned if additional forecast dimensions are introduced later.

### Implementation Constraints

- Keep live movement ownership in `driver-location-generator`.
- Use PostgreSQL for durable scenario config, run metadata, profiles, metrics, and final results.
- Treat Kafka as the inter-service contract boundary.
- Keep the initial implementation focused on synchronous orchestration plus event publication and consumption hooks; avoid embedding long-running scheduling assumptions into the controller.

## Package Structure

```text
simulation-service
├── config
├── controller
├── domain
├── entity
├── kafka
│   ├── configuration
│   ├── consumer
│   └── model
├── repository
└── service
    └── serviceimpl
```

## Java Classes And Interfaces

- `SimulationController`
- `ScenarioEngine`
- `ScenarioEngineImpl`
- `ScenarioContext`
- `PopulationEngine`
- `PopulationEngineImpl`
- `PassengerPopulationGenerator`
- `PassengerPopulationGeneratorImpl`
- `DriverPopulationGenerator`
- `DriverPopulationGeneratorImpl`
- `PassengerAgent`
- `DriverAgent`
- `PassengerDecisionEngine`
- `PassengerDecisionEngineImpl`
- `DriverDecisionEngine`
- `DriverDecisionEngineImpl`
- `RideRequestGenerator`
- `RideRequestGeneratorImpl`
- `RideRequestPublisher`
- `RideRequestPublisherImpl`
- `RideResponseConsumer`
- `DriverLocationService`
- `DriverLocationServiceImpl`
- `SimulationMetricsCollector`
- `SimulationMetricsCollectorImpl`
- `ForecastCalculator`
- `ForecastCalculatorImpl`
- `SimulationResultBuilder`
- `SimulationResultBuilderImpl`
- `SimulationResultRepository`

## Kafka Event Schemas

### Kafka Input

- `DriverLocationUpdatedEvent`
```json
{
  "driverId": "driver-1001",
  "providerIdentifier": "driver-1001",
  "scenario": "CONCERT_RAIN",
  "status": "CRUISING",
  "zone": "WEMBLEY_EVENT_ZONE",
  "latitude": 51.5561,
  "longitude": -0.2795,
  "available": true,
  "tickSequence": 44,
  "occurredAt": "2026-06-18T21:30:00Z"
}
```

- `DriverNotifiedEvent`
```json
{
  "rideId": "ride-210",
  "driverId": "driver-1001",
  "passengerId": "passenger-77",
  "pickupLatitude": 51.5569,
  "pickupLongitude": -0.2799,
  "destinationLatitude": 51.4700,
  "destinationLongitude": -0.4543,
  "expectedFare": 38.50,
  "occurredAt": "2026-06-18T21:30:05Z"
}
```

- `RideAssignedEvent`
- `RideCancelledEvent`
- `DriverReachedPickupEvent`
- `DriverReachedDestinationEvent`

### Kafka Output

- `ScenarioStartedEvent`
```json
{
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "scenario": "AIRPORT_RUSH",
  "startedAt": "2026-06-18T06:30:00Z"
}
```

- `DriverGeneratedEvent`
```json
{
  "driverId": "driver-1001",
  "driverDisplayId": "DRV-DRIVER-1001",
  "scenario": "AIRPORT_RUSH"
}
```

- `PassengerGeneratedEvent`
```json
{
  "passengerId": "passenger-77",
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "originZone": "CENTRAL_LONDON",
  "destinationZone": "HEATHROW_CORRIDOR",
  "urgencyScore": 0.92,
  "createdAt": "2026-06-18T06:31:00Z"
}
```

- `SimulatedRideRequestedEvent`
```json
{
  "rideId": "ride-210",
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "passengerId": "passenger-77",
  "pickupLatitude": 51.5099,
  "pickupLongitude": -0.1181,
  "destinationLatitude": 51.4700,
  "destinationLongitude": -0.4543,
  "requestedAt": "2026-06-18T06:31:10Z"
}
```

- `DriverAcceptedEvent`
- `DriverRefusedEvent`
- `SimulationMetricsUpdatedEvent`
- `SimulationCompletedEvent`

## Database Schema

The minimal PostgreSQL schema is captured in [simulation-service-schema.sql](/Users/hendafarhani/Documents/microGo/simulation-service/src/main/resources/sql/simulation-service-schema.sql) and includes:

- `scenario_config`
- `simulation_run`
- `simulation_result`
- `passenger_profile`
- `driver_profile`
- `simulation_metrics`

## Decision Algorithms

### Passenger Decision Algorithm

1. Start with scenario cancellation baseline.
2. Increase risk when rain, traffic, or wait time exceed thresholds.
3. Increase urgency for airport passengers near departure windows.
4. Clamp the final cancellation risk to `0.0..1.0`.

### Driver Decision Algorithm

1. Estimate pickup distance from the latest known driver coordinate and pickup coordinate.
2. Build an acceptance score from:
   - pickup distance penalty
   - fatigue penalty
   - destination preference bonus
   - fare bonus
   - airport preference bonus for `AIRPORT_RUSH`
   - Wembley-distance refusal penalty for `CONCERT_RAIN`
   - traffic and rain penalty
3. Convert the score to an acceptance probability.
4. Accept when probability is at or above the configured threshold, otherwise refuse.

## Sequence Flow With Driver Location Generation Agent

1. `simulation-service` starts a run and creates driver profiles.
2. It publishes `DriverGeneratedEvent`.
3. `driver-location-generator` creates its local identity profile, then seeds and moves drivers.
4. `ride-request` idempotently projects the generated driver into its MySQL `DRIVER` table.
5. Redis GEO `vehicle_location` is updated by `driver-location-generator`.
6. `DriverLocationUpdatedEvent` is emitted.
7. `simulation-service` updates its live driver awareness from the event stream.

## Sequence Flow With Ride Request Service

1. `simulation-service` publishes `SimulatedRideRequestedEvent`.
2. `ride-request` resolves the five nearest available drivers from Redis GEO.
3. `ride-request` publishes `DriverNotifiedEvent` for each candidate.
4. `simulation-service` decides acceptance or refusal and publishes `DriverAcceptedEvent` or `DriverRefusedEvent`.
5. `ride-request` publishes `RideAssignedEvent` when a driver is selected.
6. `simulation-service` tracks downstream lifecycle completion and cancellations for metrics.

## Output Contract For Optimization Service

`simulation_result` and `SimulationCompletedEvent` should expose:

- `activeScenario`
- `predictedDemandByZone`
- `pendingRideRequests`
- `driverAcceptanceProbability`
- `averageWaitingTimeSeconds`
- `cancellationRisk`
- `metricsSnapshot`
- `completedAt`

## Test Plan

### Unit Tests

- `DriverDecisionEngineTest`
  - prefers airport rides with better fare during `AIRPORT_RUSH`
  - refuses distant Wembley pickups under `CONCERT_RAIN` when fatigue is high
- `PassengerDecisionEngineTest`
  - raises cancellation risk for rain plus congestion
- `ScenarioEngineTest`
  - starts a run, persists the run entity, publishes bootstrap events, and persists a result

### Integration Tests

- PostgreSQL repository test for scenario, run, profile, metric, and result persistence
- Kafka test covering `DriverNotifiedEvent` to `DriverAcceptedEvent` or `DriverRefusedEvent`
- Redis-backed test ensuring live driver coordinates can be consumed without owning movement state

## Minimal Implementation Skeleton

The module includes:

- Spring Boot application and controller
- JPA entities and repositories
- Kafka event models, publishers, and listeners
- scenario orchestration and population services
- decision engines and metric collectors
- SQL schema reference

This skeleton is intentionally thin on production scheduling and external configuration rollout; those pieces should be completed alongside centralized config, Docker Compose, and deployment wiring in a follow-up integration pass.

For maintainability and easier debugging, the service now follows the same inversion-of-control style as the other microservices in this repo: contracts live under `service/`, concrete beans live under `service/serviceimpl/`, and web or Kafka edge components depend on interfaces rather than implementation classes.
