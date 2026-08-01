# Jackson 2 → Jackson 3 migration status (microGo)

Spring Boot 4's framework serialization (REST responses and STOMP/WebSocket message converters)
uses Jackson 3 (`tools.jackson.databind`). A Jackson 2 type
(`com.fasterxml.jackson.databind.JsonNode` / `ObjectNode` / `ObjectMapper`) that reaches
framework serialization is **not** treated as a JSON tree — Jackson 3 silently serializes
its getters instead of the content (e.g. `"data":{"array":false,"nodeType":"OBJECT",...}`
instead of `{"status":"ACCEPTED",...}`). It fails silently on the happy path, and a
Jackson 3 client converter cannot deserialize the payload back. This corrupted every
WebSocket payload in `dashboard-service`.

This document tracks the monorepo-wide migration. Every service is on
`spring-boot-starter-parent` **4.0.2**, so Jackson 3 (`tools.jackson` 3.0.4) is on every
classpath already.

## Status at a glance

| Service | State | Where |
|---|---|---|
| `dashboard-service` | Migrated, **PR not merged** | [PR #1](https://github.com/hendafarhani/dashboard-service/pull/1) |
| `outbox-publisher-service` | Migrated, **PR not merged** | [PR #4](https://github.com/hendafarhani/outbox-publisher-service/pull/4) |
| `optimization-service` | Migrated, **PR not merged** | [PR #2](https://github.com/hendafarhani/optimization-service/pull/2) |
| `simulation-service` | Migrated, **PR not merged** | [PR #2](https://github.com/hendafarhani/simulation-service/pull/2) |
| `ride-request` | Migrated, **PR not merged** | [PR #9](https://github.com/hendafarhani/ride-request/pull/9) |
| `driver-location-generator` | Not migrated — intentional | see [group 6](#6-driver-location-generator-driver-location-streamer-location-saver--not-migrated) |
| `driver-location-streamer` | Not migrated — intentional | see [group 6](#6-driver-location-generator-driver-location-streamer-location-saver--not-migrated) |
| `location-saver` | Not migrated — intentional | see [group 6](#6-driver-location-generator-driver-location-streamer-location-saver--not-migrated) |

**No migration has landed on any service's `main` yet.** All five are open PRs.

---

## Two findings that change how you read this document

### 1. Jackson 2 is not a declared dependency anywhere

No service declares `com.fasterxml.jackson` in its `pom.xml`, and none declares
`jackson-datatype-jsr310`. Jackson 2 (2.20.2) arrives **transitively through the Eureka
client** (`spring-cloud-starter-netflix-eureka-client` → `spectator-api` →
`jackson-databind`). Every Jackson 2 import in the monorepo was compiling against a
dependency nobody asked for, which would vanish the moment that transitive path changed.

This is why the migrations went all the way to zero `com.fasterxml` references per service
rather than stopping at the DTO layer.

### 2. `FAIL_ON_NULL_FOR_PRIMITIVES` flips default between Jackson 2 and Jackson 3

This is the one behavioural difference in the whole migration, and it is a **production**
concern, not a test detail.

| | Jackson 2 | Jackson 3 |
|---|---|---|
| `FAIL_ON_NULL_FOR_PRIMITIVES` | disabled | **enabled** |

The Kafka event models are primitive-heavy (`double latitude`, `double longitude`,
`boolean available`, `long tickSequence`). Under the Jackson 3 default, a producer that
omits any of those fields makes the consumer throw:

```
MismatchedInputException: Cannot map `null` into type `boolean`
  (through reference chain: DriverLocationUpdatedEvent["available"])
```

That silently breaks the additive schema-evolution tolerance these listeners depend on.
`optimization-service`'s `InfrastructureMappingTest.locationEventsAllowAdditiveCompatibilityFields`
exists to assert exactly that property, and it failed on the first straight port.

**Any Kafka listener mapper you migrate must disable the feature explicitly:**

```java
JsonMapper.builder()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)  // Jackson 2 parity
    .build();
```

---

## Global rules — apply in every file you touch

### Import swaps

| Jackson 2 (`com.fasterxml.jackson…`) | Jackson 3 (`tools.jackson…`) |
|---|---|
| `com.fasterxml.jackson.databind.JsonNode` | `tools.jackson.databind.JsonNode` |
| `com.fasterxml.jackson.databind.node.ObjectNode` | `tools.jackson.databind.node.ObjectNode` |
| `com.fasterxml.jackson.databind.ObjectMapper` | `tools.jackson.databind.ObjectMapper` |
| `com.fasterxml.jackson.databind.DeserializationFeature` | `tools.jackson.databind.DeserializationFeature` |
| `com.fasterxml.jackson.core.JsonProcessingException` | **delete** — replaced by unchecked `tools.jackson.core.JacksonException` |
| `com.fasterxml.jackson.datatype.jsr310.JavaTimeModule` | **delete** — java-time is built into Jackson 3 databind |

Keep the `tools.jackson` imports in the slot the `com.fasterxml` ones occupied; that keeps
diffs to one line per import.

### Exception handling

Jackson 3's `readValue` / `readTree` / `writeValueAsString` / `writeValueAsBytes` throw
**unchecked** `JacksonException`:

- Remove `throws JsonProcessingException` from method signatures.
- Delete `catch (JsonProcessingException …)` / `catch (IOException …)` blocks that only
  existed for these calls — **unless** they deliberately translate to a domain exception
  (`SerializationException`, `IllegalStateException`), in which case keep the catch and
  retype it to `tools.jackson.core.JacksonException`. Every service in this repo does the
  latter, so almost nothing was actually deleted.
- Drop the now-unused `import java.io.IOException;`.

### Constructors

`new ObjectMapper()` → `new tools.jackson.databind.json.JsonMapper()`, or
`JsonMapper.builder()…build()` when you need feature config.
`.findAndRegisterModules()` is unnecessary — Jackson 3 auto-discovers modules via
ServiceLoader.

Note: `tools.jackson.databind.ObjectMapper` is still a concrete, instantiable class in
Jackson 3.0.4 — it is fine as a *field/parameter type*. `JsonMapper` is just the canonical
JSON entry point for *construction*.

### Testing gotchas

`JacksonException`'s constructors are `protected`, so a test that needs to throw one uses
an anonymous subclass: `throw new JacksonException("boom") {};`.

Two patterns break in a way an import swap will not fix:

- **Stubbing a mapper to fail.** A stub overriding `writeValueAsString` to throw a checked
  `JsonProcessingException` no longer compiles — the method declares only the unchecked
  `JacksonException`.
- **Mockito `thenThrow(new IOException(...))`.** Mockito rejects this once `readValue` stops
  declaring `IOException`: *"Checked exception is invalid for this method."* Throw a
  `JacksonException` instead.

### Unchanged API

`asText()`, `asInt()`, `asLong()`, `asBoolean()`, `asDouble()`, `path()`, `isTextual()`,
`createObjectNode()`, chainable `put(...)`, `readTree(...)` all still exist. Method bodies
rarely change beyond exception cleanup.

### Verify

`mvn clean verify` per service (Docker must be running for the Testcontainers tests).
Always capture a **baseline run on `main` first** — `ride-request` has two integration
tests that fail identically before and after the migration for environment reasons, and
without a baseline you would misread them as regressions.

---

## 1. `dashboard-service` — the only live bug

⚠️ **The monorepo submodule pin is not simply stale — there is no fixed `main` to move it to.**

- `dashboard-service` `main` is `7ab7335`, and it does **not** contain the fix.
- The fix is commit `e29669e` ("fix: migrate JsonNode payloads to Jackson 3 so WebSocket
  data isn't corrupted"), which exists **only** on the unmerged
  `claude/dashboard-service-test-split` branch (PR #1, tip `b0791c3`).
- The monorepo currently pins `13184a4`, which is itself a commit on that PR branch —
  *ahead* of `main` but *before* the fix. So the gitlink already points off-`main`.

Therefore:

- [ ] **Merge PR #1 first.** Until it lands there is no `main` SHA that contains the fix,
      and bumping the gitlink to `b0791c3` would pin the monorepo to an unmerged branch.
- [ ] Then `git -C dashboard-service checkout <new-main-sha>`, `git add dashboard-service`
      from the monorepo root, and commit the gitlink bump.
- [ ] `mvn clean verify` in `dashboard-service` at the new SHA.

Files the fix touches: `domain/{OutboxEventEnvelope,DashboardProjection,RideDashboardMessage,ResolvedDashboardEvent}.java`,
`mapper/{RideDashboardMessageMapper,DashboardProjectionMapper}.java`,
`service/serviceimpl/OutboxEventResolverImpl.java`,
`kafka/publisher/impl/DashboardAckPublisherImpl.java`.

---

## 2. `outbox-publisher-service` — migrated (PR #4)

`OutboxEventEnvelope.payload` was a Jackson 2 `JsonNode` on a `record` — safe only while
hand-serialized to Kafka by a matching Jackson 2 mapper, and corrupting the instant
anything returned that record from a controller. Same envelope shape `dashboard-service`
exposes.

Covered: `domain/OutboxEventEnvelope`, `service/serviceimpl/OutboxEventEnvelopeFactoryImpl`,
`kafka/handler/DashboardAckHandler`, and four test classes (`JavaTimeModule` dropped).
Verified 13 tests passing, including the Testcontainers Kafka + MySQL round-trip.

---

## 3. `optimization-service` — migrated (PR #2)

REST response `OptimizationRunView` has no `JsonNode`; nothing crossed the wire. Migrated
for single-stack consistency and to stop depending on Eureka's transitive Jackson 2.

Covered: `mapper/DriverSnapshotMapper`, `service/impl/DriverSnapshotReaderImpl`,
`mapper/OptimizationResultMapper`, `kafka/configuration/{KafkaProducerConfiguration,KafkaListenerConfiguration}`,
test `InfrastructureMappingTest`. **This is the service where the
`FAIL_ON_NULL_FOR_PRIMITIVES` regression surfaced.** Verified 15 tests passing.

---

## 4. `simulation-service` — migrated (PR #2)

REST returns JPA entities with `String` JSON columns — no `JsonNode` anywhere.

Covered: `service/serviceimpl/SimulationResultBuilderImpl`,
`kafka/configuration/{KafkaProducerConfiguration,KafkaListenerConfiguration}`,
test `InfrastructureMappingTest`. The same `FAIL_ON_NULL_FOR_PRIMITIVES` guard was applied
pre-emptively — the event model is identical to `optimization-service`'s.
Verified 17 tests passing.

---

## 5. `ride-request` — migrated (PR #9)

No HTTP/WS surface (event-driven); payloads are built as `String` columns.

Covered: `mapper/EventOutBoxMapper`, `service/serviceimpl/EventOutboxServiceImpl`,
`kafka/serialization/{RideRequestJsonDeserializer,DriverGeneratedEventJsonDeserializer}`,
and six test classes — including the two rework cases described under
[Testing gotchas](#testing-gotchas).

Verified 112 tests run / 0 failures / 2 errors, **identical to the `main` baseline**. Both
errors are `rabbitmq:3-management` Testcontainers port-binding timeouts
(`RideAcceptanceRabbitMqIntegrationTest`, `RideRequestKafkaFlowIntegrationTest`),
pre-existing and unrelated to Jackson.

---

## 6. `driver-location-generator`, `driver-location-streamer`, `location-saver` — not migrated

These have **no wire risk**: the two WebSocket senders emit a plain POJO / a bare `String`,
and every `ObjectMapper` is a locally-constructed `new ObjectMapper()` inside a Kafka
`Deserializer` or a Redis store. Kafka/Redis serde is fully app-controlled and never
reaches Spring's Jackson 3 serializer.

**Impact of leaving them:** no correctness risk today, but two consequences worth knowing.
First, these three keep relying on Jackson 2 arriving transitively via the Eureka client
(see finding 1) — a dependency nobody declares, so a Eureka/Spring Cloud bump could break
their compile with no Jackson change of their own. Second, the monorepo stays
mixed-stack, so "does this module use Jackson 2 or 3?" remains a per-service question
rather than a settled one.

If you migrate them later, the files are:

- `driver-location-generator`: `kafka/configuration/{KafkaListenerConfiguration,KafkaProducerConfiguration}.java`,
  `store/RedisGeoDriverStateStore.java`.
- `driver-location-streamer`: `kafka/serialization/{DriverLocationUpdatedEventJsonDeserializer,RiderDataJsonDeserializer}.java`
  — note the no-arg `new ObjectMapper().findAndRegisterModules()` → `new JsonMapper()`.
- `location-saver`: `kafka/serialization/RiderDataJsonDeserializer.java`.

Apply the `FAIL_ON_NULL_FOR_PRIMITIVES` guard to any listener mapper you touch.

---

## Suggested merge order

1. **`dashboard-service` PR #1** — the only live bug, and it unblocks the submodule bump.
2. **`outbox-publisher-service` PR #4** — shares the `OutboxEventEnvelope` shape; review alongside #1.
3. `optimization-service` #2 / `simulation-service` #2 / `ride-request` #9 — independent, any order.
4. Monorepo gitlink bumps once the above are on their respective `main` branches.
