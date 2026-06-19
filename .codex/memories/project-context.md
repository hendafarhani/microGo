# Project Context Memory

## What microGo Is

`microGo` is a ride-sharing backend organized as multiple Spring Boot services plus deployment infrastructure. It combines synchronous platform pieces with asynchronous ride-processing flows.

## Main Services

- `ride-request`: ride lifecycle, matching, outbox creation, rider offer flow, RabbitMQ response handling
- `location-saver`: consumes and stores location data, including Redis-backed lookup support
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
- MySQL stores durable ride and user state.
- Outbox-based publication is a first-class pattern in the repo.

## Repo Notes

- The root repo acts as a coordination layer across services and infra.
- Some directories are independently managed units or submodules.
- Existing local modifications were present in `ride-request` and `location-saver` when this memory was created.

## Documentation References

- `README.md`: project overview
- `LOCAL_TESTING.md`: repeatable local validation flow
- `docs/`: architecture and deployment notes
