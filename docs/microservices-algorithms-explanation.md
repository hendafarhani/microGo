# Algorithms In Three microGo Services
## Math-Teacher Explanation For An IT Student

## How To Read This Document

For each microservice, we study:

1. the algorithm idea,
2. the concrete methods that implement it,
3. the mathematical model behind those methods,
4. why it is useful,
5. and whether it is machine learning.

---

# 1) simulation-service

## 1.1 Driver Acceptance Utility Algorithm

**Where:** `DriverDecisionBusinessRules`

### Core idea
This algorithm computes a utility-like score for a driver receiving a ride offer.
It then clamps that score to `[0, 1]` and uses a threshold to decide accept or refuse.

### Mathematical form

- Raw score:

`raw = base + sum(adjustments)`

- Probability-like score:

`p_accept = clamp(raw, 0, 1)`

- Decision:

`accept if p_accept >= threshold`

This is a hand-crafted linear score, not a learned probabilistic model.

### Methods and interpretation

- `evaluate(context)`
  - Orchestrates all sub-calculations.
  - Produces acceptance boolean, score, and refusal reason.

- `calculatePickupDistanceKm(context)` and distance helpers
  - Uses Haversine distance over latitude/longitude.
  - Geometric computation of shortest spherical path approximation.

- `pickupDistancePenalty(distanceKm)`
  - Penalty increases with distance, capped to avoid unbounded impact.

- `fatigueAdjustment(...)`, `reliabilityAdjustment(...)`, `fareAdjustment(...)`
  - Weighted linear terms:
    - fatigue lowers score,
    - reliability raises score,
    - fare raises score with a cap.

- Scenario and condition terms:
  - `destinationPreferenceAdjustment(...)`
  - `airportRushAdjustment(...)`
  - `concertRainAdjustment(...)`
  - `trafficAdjustment(...)`
  - `weatherAdjustment(...)`

- `determineRefusalReason(...)`
  - Priority-rule explanation logic (first matching reason wins).

### Why this works
It is interpretable, easy to tune, and deterministic.

### Limitation
Weights are manually chosen and may not generalize to real data shifts.

---

## 1.2 Passenger Cancellation Risk Algorithm

**Where:** `PassengerCancellationBusinessRules`

### Core idea
Compute cancellation risk from context and passenger behavior profile.

### Mathematical form

`risk = clamp(baseScenario + sensitivity + weather + traffic + waiting + airportUrgency, 0, 1)`

### Methods and interpretation

- `calculateRisk(...)`
  - Aggregates all risk factors and clamps output.

- `baseScenarioRisk(...)`
  - Scenario multiplier scales baseline risk.

- `passengerSensitivityAdjustment(...)`
  - Individual trait adjustment.

- `weatherAdjustment(...)`, `trafficAdjustment(...)`
  - Event/environment risk increments.

- `waitingTimeAdjustment(waitingTimeSeconds)`
  - Piecewise threshold behavior for long waits.

- `airportUrgencyAdjustment(...)`
  - Extra urgency-based risk only in airport scenario.

### Why this works
Quick to compute and explicit in behavior.

### Limitation
No data-driven calibration of probabilities.

---

## 1.3 Demand Forecast Shaping Algorithm

**Where:** `DemandForecastBusinessRules`

### Core idea
Create a scenario-dependent demand-by-zone map using deterministic splits.

### Methods and interpretation

- `buildPredictedDemandByZone(...)`
  - Builds base map with anchor zone and scenario pattern.

- `addAirportRushDemandShape(...)`
  - Emphasizes Heathrow and Central London.

- `addConcertDemandShape(...)`
  - Emphasizes Wembley and General London.

- `minimumDemand(...)`
  - Applies floor `max(1, demand)` for stability.

### Why this works
Ensures optimization always receives a usable demand profile.

### Limitation
It is heuristic demand shaping, not statistical forecasting.

---

# 2) optimization-service

## 2.1 Zone Demand/Supply Accounting Algorithm

**Where:** `ZoneDemandBusinessRules`

### Core idea
Compute how much demand exists per zone and how much supply can serve it.

### Methods and interpretation

- `buildDemandByZone(snapshot)`
  - Combines predicted demand with pending ride request counts.

- `buildSupplyByZone(snapshot, targetZones)`
  - Counts repositionable drivers in effective zones.

- `totalDemand(...)`
  - Sum with floor to avoid divide-by-zero.

- `zoneShortage(...)`, `countUnmetRequests(...)`, `maximumZoneShortage(...)`
  - Shortage math across zones.

- `highDemandCoverageRatio(...)`
  - Coverage quality on top-demand zones.

### Why this works
Transforms raw state into optimization-ready pressure metrics.

---

## 2.2 Baseline-vs-Optimized Metrics Algorithm

**Where:** `OptimizationMetricsBusinessRules`

### Core idea
Map demand/supply pressures into business KPIs.

### Methods and interpretation

- `calculateMetrics(snapshot, demandByZone, supplyByZone)`
  - Computes:
    - average wait estimate,
    - p95 wait estimate,
    - cancellation risk estimate,
    - high-demand coverage,
    - unmet requests,
    - central London supply.

### Mathematical style
Piecewise and weighted deterministic functions with clipping for bounded risk.

### Why this works
Gives comparable KPI outputs for baseline and recommended plans.

### Limitation
KPI equations are engineered, not fit from historical outcomes.

---

## 2.3 Dispatch Constraint Scoring Algorithm

**Where:** `RideDispatchConstraintBusinessRules`

### Core idea
Filter and score candidate driver-to-zone decisions under operational constraints.

### Methods and interpretation

- `eligibleDrivers(snapshot)`
  - Orders by high acceptance and low fatigue.

- `isReachable(...)`
  - Time-threshold feasibility constraint.

- `wouldOvercrowd(...)`
  - Capacity/over-saturation guard.

- `wouldBreakCentralSupply(...)`
  - Preserves minimum central reserve.

- `priorityScore(...)`
  - Weighted utility:
    - demand pressure bonus,
    - acceptance bonus,
    - fatigue penalty,
    - travel-time penalty.

### Why this works
Combines feasibility and utility in a fast deterministic scorer.

---

## 2.4 Repositioning Utility/Outcome Algorithm

**Where:** `DriverRepositioningBusinessRules`

### Core idea
Estimate quality and impact of each repositioning action.

### Methods and interpretation

- `calculateTargetZoneScore(...)`
  - Combines base priority with shortage reward and same-zone penalty.

- `calculateExpectedWaitReductionSeconds(...)`
  - Converts travel effect into wait-improvement estimate with a floor.

- `calculateExpectedCancellationReduction(...)`
  - Bounded cancellation improvement estimate.

- `calculateComparisonScore(comparison)`
  - Scalar objective from wait and cancellation deltas.

### Why this works
Enables ranking and summarizing improvement with interpretable formulas.

---

## 2.5 Simulation Demand Inference Algorithm

**Where:** `SimulationDemandBusinessRules`

### Core idea
Infer fallback demand shape and traffic multipliers by scenario.

### Methods and interpretation

- `inferDemandByScenario(...)`
  - Scenario-specific demand templates.

- `resolveTrafficMultiplier(...)`
  - Scenario-to-multiplier mapping.

### Why this works
Keeps optimization robust when richer upstream state is unavailable.

---

# 3) driver-location-generator

## 3.1 Deterministic Driver Seeding Algorithm

**Where:** `DriverSeedBusinessRules`

### Core idea
Assign initial positions reproducibly using driver-id hash and scenario spread.

### Methods and interpretation

- `initialPosition(driverId, config, defaultLatitude, defaultLongitude)`
  - If no scenario config, return default center.
  - Else hash driver id, map to centered buckets, scale by spread, apply offsets.

### Why this works
Reproducible simulations and controlled spatial diversity.

---

## 3.2 Geodesic Distance and Interpolation Algorithm

**Where:** `LondonDistanceBusinessRules`

### Core idea
Measure geographic distance and move toward targets by fixed step sizes.

### Methods and interpretation

- `distanceMeters(origin, destination)`
  - Haversine formula in meters.

- `moveToward(origin, destination, stepMeters)`
  - Snap to destination if reachable in one step.
  - Otherwise linear interpolation with ratio `step / distance`.

### Why this works
Stable, realistic movement progression over geographic coordinates.

---

## 3.3 Traffic-Adjusted Step Model

**Where:** `LondonTrafficBusinessRules`

### Core idea
Actual movement step depends on status, scenario pressure, and zone congestion.

### Methods and interpretation

- `stepMeters(state, properties)`
  - `baseStep(status) * scenarioMultiplier * zoneMultiplier`

- internal base-step logic by `DriverStatus`
  - Reposition/pickup/trip/idle/offline different movement capacities.

- scenario multiplier and zone multiplier helpers
  - Encode congestion and context effects.

### Why this works
Captures heterogeneous movement behavior while remaining deterministic.

---

## 3.4 Zone Classification Algorithm

**Where:** `LondonZoneBusinessRules`

### Core idea
Map coordinate points into named operational zones.

### Methods and interpretation

- `resolveZone(point)`
  - Ordered zone checks:
    - Wembley,
    - Heathrow,
    - Central London,
    - fallback General London.

- zone-boundary helper
  - Axis-aligned range checks around zone centers.

### Why this works
Fast deterministic zoning with clear priority semantics.

---

## 3.5 Movement Progress Rule Algorithm

**Where:** `MovementProgressBusinessRules`

### Core idea
Determine target arrival and choose next position.

### Methods and interpretation

- `hasReachedTarget(...)`
  - Distance threshold predicate.

- `resolveTargetProgressPosition(...)`
  - Snap-or-move behavior.

- `buildCruisingDriftTarget(...)`
  - Drift vector for idle/casual cruising.

- `buildScenarioDemandBiasTarget(...)`
  - Anchor target for scenario-focused movement.

### Why this works
Unifies movement progression logic across multiple movement strategies.

---

# Final Classification: Are these ML algorithms?

No. These are mainly:

- deterministic rule-based heuristics,
- geospatial geometry algorithms,
- and constrained optimization scoring rules.

They are not machine-learning models because:

- no training process exists,
- no learned parameters are fitted from data,
- and outputs are fully determined by current inputs and fixed coefficients.

---

# If You Want To Upgrade Toward ML Later

A safe path is:

1. keep current rule outputs as baseline features,
2. log outcomes and labels,
3. train calibrated acceptance/cancellation models,
4. compare against current heuristic baselines in replay/simulation,
5. deploy only if out-of-sample gains are stable.

