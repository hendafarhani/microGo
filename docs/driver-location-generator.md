# Driver Location Generator

## Architecture Proposal

`driver-location-generator` should be a dedicated microservice that owns live driver movement state. It consumes driver lifecycle and movement commands from Kafka, updates Redis GEO plus current geo-state records, and publishes movement events for downstream services such as `driver-location-streamer`, simulation consumers, and `optimization-service`.

The service boundary is intentionally separate from `location-saver` and `driver-location-streamer`:

- `driver-location-generator` owns driver simulation and movement decisions.
- `location-saver` can remain focused on Redis-backed persistence for existing location flows.
- `driver-location-streamer` remains a projection service for WebSocket or UI consumers.
- `ride-request` keeps reading the Redis GEO key `vehicle_location`, so the nearest-driver lookup path stays compatible.
- `optimization-service` reads Redis GEO and current geo-state, but only publishes recommendations. It does not execute movement.

## Event And Data Flow

1. `DriverGeneratedEvent` seeds `DriverGeoState` for a driver in a London scenario.
2. `DriverMovementScheduler` advances all active drivers every few seconds.
3. `DriverMovementEngine` resolves the active movement strategy from the driver state.
4. `RedisGeoDriverStateStore` writes:
   - Redis GEO member in `vehicle_location`
   - serialized current state in `driver:geo-state:{driverId}`
   - zone cache in `driver:zone:{driverId}`
5. `DriverLocationPublisher` emits:
   - `DriverLocationUpdatedEvent`
   - `DriverEnteredZoneEvent`
   - `DriverReachedPickupEvent`
   - `DriverReachedDestinationEvent`
   - `DriverBecameAvailableEvent`

## Package Structure

```text
driver-location-generator
├── config
├── domain
├── kafka
│   ├── configuration
│   ├── handler
│   └── model
├── service
│   └── strategy
└── store
```

## Core Classes And Interfaces

- `DriverGeoState`: source-of-truth live state for one driver
- `DriverMovementEngine`: applies commands, advances ticks, persists state, publishes events
- `DriverMovementScheduler`: periodic movement trigger
- `LondonZoneService`: maps coordinates to Wembley, Heathrow, Central, or General London zones
- `LondonDistanceMatrix`: distance and movement interpolation helper
- `LondonTrafficModel`: converts scenario and zone pressure into step size multipliers
- `MovementStrategyResolver`: picks the active movement behavior from driver status
- `IdleMovementStrategy`
- `ScenarioMovementStrategy`
- `RepositioningMovementStrategy`
- `PickupMovementStrategy`
- `TripMovementStrategy`
- `RedisGeoDriverStateStore`: Redis GEO plus state serialization
- `DriverLocationPublisher`: Kafka publisher for output events

## Kafka Event Schemas

### Input Topics

- `DriverGeneratedEvent`
```json
{
  "driverId": "driver-101",
  "driverDisplayId": "DRV-DRIVER-101",
  "scenario": "CONCERT_RAIN"
}
```

- `RepositionDriverCommand`
```json
{
  "driverId": "driver-101",
  "targetLatitude": 51.556,
  "targetLongitude": -0.2796
}
```

- `MoveToPickupCommand`
```json
{
  "driverId": "driver-101",
  "rideId": "ride-55",
  "pickupLatitude": 51.5033,
  "pickupLongitude": -0.1195
}
```

- `StartTripCommand`
```json
{
  "driverId": "driver-101",
  "rideId": "ride-55",
  "destinationLatitude": 51.4700,
  "destinationLongitude": -0.4543
}
```

- `StopDriverCommand`
```json
{
  "driverId": "driver-101"
}
```

### Output Topics

- `DriverLocationUpdatedEvent`
```json
{
  "driverId": "driver-101",
  "scenario": "CONCERT_RAIN",
  "status": "CRUISING",
  "zone": "WEMBLEY_EVENT_ZONE",
  "latitude": 51.5570,
  "longitude": -0.2804,
  "available": true,
  "tickSequence": 12,
  "occurredAt": "2026-06-18T12:00:00Z"
}
```

- `DriverEnteredZoneEvent`
- `DriverReachedPickupEvent`
- `DriverReachedDestinationEvent`
- `DriverBecameAvailableEvent`

Each uses `driverId` as the Kafka key to preserve per-driver ordering.

## Redis GEO Data Model

- GEO key: `vehicle_location`
  - member: `{driverId}`
  - coordinates: `{longitude, latitude}`
- State key: `driver:geo-state:{driverId}`
  - JSON serialized `DriverGeoState`
- Zone key: `driver:zone:{driverId}`
  - string zone name

This keeps compatibility with the existing `ride-request` lookup while adding richer state for simulation consumers.

## Movement Algorithm

1. Seed a deterministic initial coordinate from the scenario center plus a hash-based spread.
2. Resolve the driver zone from the current coordinate.
3. Resolve the active movement strategy from driver status.
4. Compute a step size using base speed and scenario or zone traffic multipliers.
5. Move toward the target or scenario demand anchor using linear interpolation.
6. Detect arrivals and emit milestone events.
7. Persist the new state and publish a `DriverLocationUpdatedEvent`.

## Scenario Rules

### `CONCERT_RAIN`

- initial cluster near Wembley Stadium
- drift bias toward the Wembley event zone
- slower movement inside the zone to simulate congestion and rain
- good for surge and queueing behavior

### `AIRPORT_RUSH`

- initial cluster along the Heathrow corridor
- drift bias toward Heathrow arrivals and outbound corridors
- moderate slowdown in the corridor to simulate airport pressure
- good for dispatch balancing and longer reposition moves

## Unit Test Plan

- `LondonZoneServiceTest`
  - resolves Wembley points to `WEMBLEY_EVENT_ZONE`
  - resolves Heathrow points to `HEATHROW_CORRIDOR`
- `MovementStrategyResolverTest`
  - selects repositioning strategy for `REPOSITIONING`
  - selects trip strategy for `ON_TRIP`
- `DriverMovementEngineTest`
  - registers a driver with deterministic initial state
  - transitions reposition command into `REPOSITIONING`
  - advances a driver and emits a location update
- `RedisGeoDriverStateStoreTest`
  - serializes and deserializes `DriverGeoState`
  - writes Redis-compatible GEO coordinates

## Risk Hotspots

- Per-driver ordering should stay keyed by `driverId`.
- Redis GEO and state JSON must remain synchronized.
- Existing downstream services currently use rider-shaped contracts; they will need a contract migration to consume the new driver events directly.
- PostgreSQL-backed stable scenario configuration is not yet implemented in this skeleton. The current version uses config-backed defaults and keeps the boundary available for future persistence.
