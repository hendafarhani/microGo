# microGo Local Testing

This guide captures the repeatable local happy-path test for the Docker Compose stack.

## Prerequisites

- Docker Desktop running.
- Repository cloned with submodules:

```bash
git submodule update --init --recursive
```

If Docker Desktop is not running on macOS:

```bash
open -a Docker
docker info
```

Wait for `docker info` to return server details before starting the stack.

## Start The Stack

From the repository root:

```bash
docker compose up --build -d
```

Wait until core services are healthy:

```bash
docker compose ps
curl -fsS http://localhost:8082
curl -fsS http://localhost:8080/ride-request/default
```

The PostgreSQL container initializes separate `driver_location`, `simulation_service`,
and `optimization_service` databases from the `POSTGRES_DATABASES` value in
`docker-compose.yml`.

PostgreSQL only runs `/docker-entrypoint-initdb.d` scripts when its data directory is
first created. If the container was initialized before a database was added, run the
initializer manually without deleting existing data:

```bash
docker compose exec postgres \
  bash /docker-entrypoint-initdb.d/01_create_service_databases.sh
docker compose restart driver-location-generator simulation-service optimization-service
```

Confirm the PostgreSQL-backed services started successfully:

```bash
docker compose ps simulation-service optimization-service
docker compose logs --tail=100 simulation-service optimization-service
docker compose exec -T postgres psql -U microgo_user -d postgres -c '\l'
```

If you are reusing an older local MySQL volume, make sure the outbox payload column is large enough for the event envelope payload:

```bash
docker compose exec -T mysql mysql -umicrogo_user -ppassword ride_requests_db \
  -e "ALTER TABLE event_outbox MODIFY payload LONGTEXT NOT NULL;"
```

Older case-sensitive MySQL volumes may contain both `DRIVER` and `driver`. Merge the
legacy uppercase table into the canonical lowercase table without deleting driver
records:

```bash
docker compose exec -T mysql mysql -umicrogo_user -ppassword ride_requests_db \
  < mysql-init/03_migrate_driver_table_case.sql
```

If the same volume was created before `ride_request.status` used string enum values, repair the status column before testing timeout flows. This preserves existing status values and removes the old ordinal check constraint that rejects `TIMED_OUT`:

```bash
docker compose exec -T mysql mysql -umicrogo_user -ppassword ride_requests_db \
  -e "ALTER TABLE ride_request ADD COLUMN status_new varchar(32) NULL; \
      UPDATE ride_request \
      SET status_new = CASE status \
          WHEN 0 THEN 'PENDING' \
          WHEN 1 THEN 'PROCESSED' \
          WHEN 2 THEN 'CANCELED' \
          WHEN 3 THEN 'ACCEPTED' \
          WHEN 4 THEN 'TIMED_OUT' \
          ELSE NULL \
      END; \
      ALTER TABLE ride_request DROP CHECK ride_request_chk_1; \
      ALTER TABLE ride_request DROP COLUMN status; \
      ALTER TABLE ride_request CHANGE status_new status varchar(32) NULL;"
```

## Happy Scenario

The ride request flow is message-driven. The local test sends a Kafka `ride.requests` message, then verifies that:

1. `ride-request` consumes the request.
2. MySQL contains the persisted ride request and `EVENT_OUTBOX` rows.
3. `outbox-publisher-service` publishes pending outbox rows to Kafka topic `ride.request.events`.
4. `dashboard-service` validates the outbox row, reads the matching ride table, and pushes a WebSocket update.
5. `dashboard-service` publishes an acknowledgement on `ride.request.events.acks`, which marks the outbox row `PROCESSED`.

### Seed Requester And Riders

```bash
docker compose exec -T mysql mysql -umicrogo_user -ppassword ride_requests_db \
  -e "INSERT IGNORE INTO users (identifier, name) VALUES ('user-local-happy', 'Local Happy User'); \
      INSERT IGNORE INTO driver (identifier, name, license_number, date_of_birth) VALUES \
      ('rider-london-1','Olivia Parker','LON-RR-1001','1990-03-14'), \
      ('rider-london-2','Mason Reed','LON-RR-1002','1988-07-02'), \
      ('rider-london-3','Sophia Turner','LON-RR-1003','1994-01-21'), \
      ('rider-london-4','Ethan Brooks','LON-RR-1004','1992-11-05'), \
      ('rider-london-5','Ava Bennett','LON-RR-1005','1996-05-17'), \
      ('rider-london-6','Lucas Foster','LON-RR-1006','1989-09-09'), \
      ('rider-london-7','Mia Hayes','LON-RR-1007','1995-12-01'), \
      ('rider-london-8','Noah Collins','LON-RR-1008','1991-04-23'), \
      ('rider-london-9','Isla Morgan','LON-RR-1009','1993-08-30'), \
      ('rider-london-10','Leo Ward','LON-RR-1010','1987-10-12');"
```

The rider identifiers match the default London rider records used by `ride-request`.

Drivers produced by `simulation-service` no longer require manual insertion into `driver`.
`ride-request` consumes `DriverGeneratedEvent` from `driver.generated` and idempotently
creates or updates its local MySQL dispatch projection using `driverId`.

For an existing local MySQL volume created with the former `RIDERS` table, apply the
data-preserving rename once before restarting `ride-request` and `dashboard-service`:

```bash
docker compose exec -T mysql mysql -uroot -ppassword ride_requests_db \


To verify the projection:

```bash
docker compose exec -T mysql mysql -umicrogo_user -ppassword ride_requests_db \
  -e "SELECT identifier, driver_identifier, driver_display_id FROM driver ORDER BY id DESC LIMIT 10;"
```

### Publish Driver Locations

Publish rider locations through the configured Kafka location topic, then wait until `location-saver` has stored geo data in Redis:

```bash
docker compose exec -T redis redis-cli ZCARD vehicle_location
```

The result should be greater than `0`.

### Send A Ride Request

```bash
printf '%s\n' 'user-local-happy:{"userIdentifier":"user-local-happy","location":{"latitude":51.5074,"longitude":-0.1278}}' | \
docker compose exec -T kafka kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic ride.requests \
  --property parse.key=true \
  --property key.separator=:
```

### Verify Persistence And Outbox Publishing

```bash
docker compose exec -T mysql mysql -umicrogo_user -ppassword ride_requests_db \
  -e "SELECT id, identifier, status FROM ride_request ORDER BY id DESC LIMIT 5;"

docker compose exec -T mysql mysql -umicrogo_user -ppassword ride_requests_db \
  -e "SELECT id, event_type, status, ride_request_identifier, retry_count, last_error FROM event_outbox ORDER BY id DESC LIMIT 10;"
```

Expected statuses after Kafka publish are usually `PROCESSED` once `dashboard-service` is running and consuming events.

### Verify Dashboard WebSocket And Ack

Subscribe a client to the dashboard WebSocket endpoint and the ride-specific destination:

```bash
npx wscat --connect http://localhost:8087/ws/websocket
```

Then subscribe with STOMP to `/topic/ride-requests/<ride_request_identifier>` and confirm the JSON payload contains:

- `eventId`
- `eventType`
- `sourceTable`
- `payload`
- `data`

If you need to confirm the acknowledgement path directly, inspect the most recent processed outbox row:

```bash
docker compose exec -T mysql mysql -umicrogo_user -ppassword ride_requests_db \
  -e "SELECT id, event_type, status, processed_at FROM event_outbox ORDER BY id DESC LIMIT 10;"
```

Rows streamed by `dashboard-service` should move to `PROCESSED`.

## Useful Diagnostics

```bash
docker compose logs --tail=200 ride-request
docker compose logs --tail=200 outbox-publisher-service
docker compose logs --tail=200 dashboard-service
docker compose logs --tail=200 simulation-service
docker compose logs --tail=200 optimization-service
docker compose exec -T kafka kafka-topics --bootstrap-server kafka:9092 --describe --topic ride.request.events
docker compose exec -T kafka kafka-consumer-groups --bootstrap-server kafka:9092 --describe --group driver.matching.group
docker compose exec -T kafka kafka-consumer-groups --bootstrap-server kafka:9092 --describe --group dashboard-service.group
```

Expected local topic shape for `ride.request.events` is `3` partitions and replication factor `1`.

## Stop The Stack

```bash
docker compose down
```

Use `docker compose down -v` only when you intentionally want to delete local MySQL/Redis/Kafka state.
