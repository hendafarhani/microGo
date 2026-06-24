package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ZoneDemandBusinessRules {

    private static final int HIGH_DEMAND_ZONE_LIMIT = 2;

    private ZoneDemandBusinessRules() {
    }

    public static Map<ZoneId, Integer> buildDemandByZone(OptimizationSnapshot snapshot) {
        Map<ZoneId, Integer> demandByZone = zeroedZoneMap();
        for (ZoneId zoneId : ZoneId.values()) {
            demandByZone.put(zoneId, snapshot.getPredictedDemandByZone().getOrDefault(zoneId, 0));
        }
        snapshot.getPendingRideRequests().forEach(request ->
                demandByZone.merge(request.getPickupZone(), 1, Integer::sum));
        return demandByZone;
    }

    public static Map<ZoneId, Integer> buildSupplyByZone(
            OptimizationSnapshot snapshot,
            Map<String, ZoneId> targetZones) {
        Map<ZoneId, Integer> supplyByZone = zeroedZoneMap();
        snapshot.getDriverSnapshots().stream()
                .filter(DriverSnapshot::isRepositionable)
                .map(driver -> targetZones.getOrDefault(driver.getDriverId(), driver.getCurrentZone()))
                .forEach(zoneId -> supplyByZone.merge(zoneId, 1, Integer::sum));
        return supplyByZone;
    }

    public static int totalDemand(Map<ZoneId, Integer> demandByZone) {
        return Math.max(1, demandByZone.values().stream().mapToInt(Integer::intValue).sum());
    }

    public static int countUnmetRequests(
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        return demandByZone.entrySet().stream()
                .mapToInt(entry -> zoneShortage(entry.getKey(), demandByZone, supplyByZone))
                .sum();
    }

    public static int maximumZoneShortage(
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        return demandByZone.keySet().stream()
                .mapToInt(zoneId -> zoneShortage(zoneId, demandByZone, supplyByZone))
                .max()
                .orElse(0);
    }

    public static int zoneShortage(
            ZoneId zoneId,
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        return Math.max(0, demandByZone.getOrDefault(zoneId, 0) - supplyByZone.getOrDefault(zoneId, 0));
    }

    public static double highDemandCoverageRatio(
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        List<Map.Entry<ZoneId, Integer>> highDemandZones = demandByZone.entrySet().stream()
                .sorted(Map.Entry.<ZoneId, Integer>comparingByValue().reversed())
                .limit(HIGH_DEMAND_ZONE_LIMIT)
                .toList();
        int highDemandTotal = Math.max(1, highDemandZones.stream().mapToInt(Map.Entry::getValue).sum());
        int coveredDemand = highDemandZones.stream()
                .mapToInt(entry -> Math.min(
                        entry.getValue(),
                        supplyByZone.getOrDefault(entry.getKey(), 0)))
                .sum();
        return coveredDemand / (double) highDemandTotal;
    }

    private static Map<ZoneId, Integer> zeroedZoneMap() {
        Map<ZoneId, Integer> zoneMap = new EnumMap<>(ZoneId.class);
        for (ZoneId zoneId : ZoneId.values()) {
            zoneMap.put(zoneId, 0);
        }
        return zoneMap;
    }
}

