# Angular Live Map Frontend Architecture

## Scope Classification

- domain logic change
- event contract change
- local runtime change
- documentation change

## 1. Target Services And Why

- `frontend/dispatch-console` or `frontend/live-ops-ui`:
  create a separate Angular application for the London operations map. Keep map rendering, scenario selection, and local UI state in the frontend instead of adding view logic to Spring services.
- `gateway`:
  keep it as the single browser entrypoint for HTTP and WebSocket traffic, but do not place scenario or ride domain logic here. Add explicit proxy routes for the Angular app, the scenario-control HTTP API, and the WebSocket endpoints.
- `driver-location-generator`:
  own scenario selection and simulation lifecycle because it already owns `LondonScenario`, simulated driver state, and movement commands.
- `driver-location-streamer`:
  continue as the movement projection service for browser clients. Its job is to broadcast live driver positions, not to decide ride lifecycle.
- `ride-request`:
  continue owning ride creation plus driver accept or decline decisions. The browser should call HTTP endpoints here through the gateway instead of publishing RabbitMQ messages directly.
- `dashboard-service`:
  continue owning ride-request projections for browser clients, but extend it from ride-specific topics to a global live dispatch stream so the Angular app can observe all requests in one subscription.

## 2. Event And Data Flow

1. Angular loads through `gateway`.
2. Angular fetches scenario catalog over HTTP from a new controller on `driver-location-generator`, for example `GET /simulation/scenarios`.
3. Angular starts or resets a scenario over HTTP through `gateway`, for example `POST /simulation/scenarios/{scenarioKey}/start`.
4. `driver-location-generator` seeds or resets simulated providers for the chosen scenario and keeps publishing `DriverLocationUpdatedEvent` to Kafka.
5. `driver-location-streamer` consumes `driver.location.updated` and exposes a browser-facing WebSocket destination such as `/topic/driverLocations`.
6. Angular opens a second WebSocket subscription for ride lifecycle updates from `dashboard-service`.
7. `ride-request` exposes HTTP endpoints through `gateway` for:
   - creating a ride request
   - accepting a ride as a provider
   - declining a ride as a provider
8. `ride-request` persists the ride state and offer state, then keeps using the outbox flow to publish ride events.
9. `dashboard-service` consumes `ride.request.events`, projects the current ride or offer state, and publishes a global dispatch topic plus the existing ride-specific topic.
10. Angular merges both live feeds into one map state:
   - provider cars from `driver-location-streamer`
   - requester markers from ride creation or dashboard projection
   - request cards or lines from the dashboard projection
11. When a ride is accepted, Angular removes the requester marker and the matched provider car from the "available dispatch" layer.

## Recommended Contract Adjustments

- Add a stable browser-facing `providerIdentifier` field to live movement and ride projection payloads.
  The current backend uses `driverId` in movement events and `riderIdentifier` in ride offers. The frontend needs one canonical identifier to correlate the accepted provider with the moving car.
- Extend dashboard projections to include requester coordinates.
  `dashboard-service` currently sends ride status and accepted identifiers, but not the `RideRequestEntity.location` needed to place a requester on the map.
- Add a global dashboard topic such as `/topic/dispatch-events`.
  The current `/topic/ride-requests/{rideId}` shape is too narrow for an operations map that must render many live requests at once.
- Keep legacy fields during migration.
  Add new fields rather than renaming current ones immediately so downstream services and tests can move incrementally.

## 3. Risk Hotspots

- Identifier mismatch:
  `driver-location-generator` emits `driverId`, while `ride-request` and `dashboard-service` refer to `riderIdentifier`. Without a canonical provider identifier, the UI cannot reliably remove the accepted car from the map.
- Projection incompleteness:
  requester coordinates are stored in `ride-request` but are not present in the current dashboard WebSocket payload.
- Gateway WebSocket routing:
  the repo currently relies on discovery-based gateway routing. Browser WebSocket paths should be made explicit and tested through `gateway`, especially for SockJS or STOMP endpoints.
- Scenario reset semantics:
  starting a new scenario must define whether existing simulated providers are stopped, replaced, or reused. The reset behavior should be deterministic for demos and tests.
- Event ordering:
  map updates should preserve per-provider ordering on movement events and per-ride ordering on ride lifecycle events.

## 4. Implementation Constraints

- Do not move ride domain logic into `gateway`.
- Do not let the Angular app publish Kafka or RabbitMQ messages directly.
- Keep `driver-location-generator` as the owner of scenario state and simulated provider lifecycle.
- Keep `ride-request` as the owner of accept or decline decisions and persisted ride state.
- Prefer additive event schema changes over breaking renames.
- Avoid a broad `rider` to `driver` refactor as part of this feature. Introduce a browser-facing canonical field first, then clean up naming separately if desired.

## 5. Required Test Coverage

- Angular integration tests:
  scenario selection, two live subscriptions, map reconciliation, and accepted-ride removal behavior.
- `driver-location-generator` tests:
  scenario start or reset endpoint, idempotent reset behavior, and emitted provider identifiers.
- `driver-location-streamer` tests:
  WebSocket delivery through the browser-facing destination and any added snapshot or filtering behavior.
- `ride-request` tests:
  HTTP accept or decline endpoints, validation, and continued outbox publication after state changes.
- `dashboard-service` tests:
  global dispatch topic publication, requester coordinate projection, and canonical provider identifier propagation.
- end-to-end local validation:
  verify the Angular app can connect only through `gateway`, receive both WebSocket streams, submit accept or decline actions, and remove matched markers after acceptance.

## Suggested Delivery Order

1. Align provider identifiers and dashboard projection fields.
2. Add HTTP control endpoints in `driver-location-generator` and `ride-request`.
3. Expose explicit gateway routes for HTTP and WebSocket traffic.
4. Build the Angular live-ops UI against the unified contracts.
5. Extend `LOCAL_TESTING.md` with the browser-driven happy path.
