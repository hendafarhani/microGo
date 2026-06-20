package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.mapper.DistanceMatrixMapper;
import com.microgo.optimization_service.service.DistanceMatrixSnapshotReader;
import com.microgo.optimization_service.domain.DistanceMatrixFact;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class DistanceMatrixSnapshotReaderImpl implements DistanceMatrixSnapshotReader {

    private static final double WEMBLEY_LAT = 51.5560;
    private static final double WEMBLEY_LON = -0.2796;
    private static final double HEATHROW_LAT = 51.4700;
    private static final double HEATHROW_LON = -0.4543;
    private static final double CENTRAL_LAT = 51.5099;
    private static final double CENTRAL_LON = -0.1181;

    private final DistanceMatrixMapper distanceMatrixMapper;

    public DistanceMatrixSnapshotReaderImpl(DistanceMatrixMapper distanceMatrixMapper) {
        this.distanceMatrixMapper = distanceMatrixMapper;
    }

    @Override
    public DistanceMatrixFact buildCurrentDistanceMatrix(double trafficMultiplier) {
        double effectiveTrafficMultiplier = normalizeTrafficMultiplier(trafficMultiplier);
        Map<ZoneId, Map<ZoneId, Integer>> travelMinutes = new EnumMap<>(ZoneId.class);
        Map<ZoneId, Map<ZoneId, Double>> distanceKilometers = new EnumMap<>(ZoneId.class);

        addLondonRoutes(travelMinutes, distanceKilometers, effectiveTrafficMultiplier);
        mirrorRoutes(travelMinutes, distanceKilometers);

        return distanceMatrixMapper.toDistanceMatrix(travelMinutes, distanceKilometers);
    }

    private double normalizeTrafficMultiplier(double trafficMultiplier) {
        return trafficMultiplier <= 0 ? 1.0 : trafficMultiplier;
    }

    private void addLondonRoutes(
            Map<ZoneId, Map<ZoneId, Integer>> travelMinutes,
            Map<ZoneId, Map<ZoneId, Double>> distanceKilometers,
            double trafficMultiplier) {
        addRoute(travelMinutes, distanceKilometers, ZoneId.WEMBLEY_EVENT_ZONE, ZoneId.WEMBLEY_EVENT_ZONE, 8, 2.0, trafficMultiplier);
        addRoute(travelMinutes, distanceKilometers, ZoneId.WEMBLEY_EVENT_ZONE, ZoneId.CENTRAL_LONDON, 28, 14.0, trafficMultiplier);
        addRoute(travelMinutes, distanceKilometers, ZoneId.WEMBLEY_EVENT_ZONE, ZoneId.HEATHROW_CORRIDOR, 42, 29.0, trafficMultiplier);
        addRoute(travelMinutes, distanceKilometers, ZoneId.WEMBLEY_EVENT_ZONE, ZoneId.GENERAL_LONDON, 20, 9.0, trafficMultiplier);

        addRoute(travelMinutes, distanceKilometers, ZoneId.CENTRAL_LONDON, ZoneId.CENTRAL_LONDON, 7, 2.0, trafficMultiplier);
        addRoute(travelMinutes, distanceKilometers, ZoneId.CENTRAL_LONDON, ZoneId.HEATHROW_CORRIDOR, 40, 26.0, trafficMultiplier);
        addRoute(travelMinutes, distanceKilometers, ZoneId.CENTRAL_LONDON, ZoneId.GENERAL_LONDON, 16, 7.0, trafficMultiplier);

        addRoute(travelMinutes, distanceKilometers, ZoneId.HEATHROW_CORRIDOR, ZoneId.HEATHROW_CORRIDOR, 9, 3.0, trafficMultiplier);
        addRoute(travelMinutes, distanceKilometers, ZoneId.HEATHROW_CORRIDOR, ZoneId.GENERAL_LONDON, 22, 11.0, trafficMultiplier);
        addRoute(travelMinutes, distanceKilometers, ZoneId.GENERAL_LONDON, ZoneId.GENERAL_LONDON, 10, 4.0, trafficMultiplier);
    }

    @Override
    public ZoneId resolveZone(double latitude, double longitude) {
        if (distanceKm(latitude, longitude, WEMBLEY_LAT, WEMBLEY_LON) <= 4.5) {
            return ZoneId.WEMBLEY_EVENT_ZONE;
        }
        if (distanceKm(latitude, longitude, HEATHROW_LAT, HEATHROW_LON) <= 6.5) {
            return ZoneId.HEATHROW_CORRIDOR;
        }
        if (distanceKm(latitude, longitude, CENTRAL_LAT, CENTRAL_LON) <= 5.0) {
            return ZoneId.CENTRAL_LONDON;
        }
        return ZoneId.GENERAL_LONDON;
    }

    private void addRoute(Map<ZoneId, Map<ZoneId, Integer>> travelMinutes,
                          Map<ZoneId, Map<ZoneId, Double>> distanceKilometers,
                          ZoneId fromZone,
                          ZoneId toZone,
                          int baseMinutes,
                          double distanceKm,
                          double trafficMultiplier) {
        travelMinutes.computeIfAbsent(fromZone, ignored -> new EnumMap<>(ZoneId.class))
                .put(toZone, Math.max(1, (int) Math.round(baseMinutes / trafficMultiplier)));
        distanceKilometers.computeIfAbsent(fromZone, ignored -> new EnumMap<>(ZoneId.class))
                .put(toZone, distanceKm);
    }

    private void mirrorRoutes(Map<ZoneId, Map<ZoneId, Integer>> travelMinutes,
                              Map<ZoneId, Map<ZoneId, Double>> distanceKilometers) {
        for (ZoneId fromZone : ZoneId.values()) {
            for (ZoneId toZone : ZoneId.values()) {
                Integer minutes = travelMinutes.getOrDefault(fromZone, Map.of()).get(toZone);
                Double distance = distanceKilometers.getOrDefault(fromZone, Map.of()).get(toZone);
                if (minutes != null) {
                    travelMinutes.computeIfAbsent(toZone, ignored -> new EnumMap<>(ZoneId.class)).putIfAbsent(fromZone, minutes);
                }
                if (distance != null) {
                    distanceKilometers.computeIfAbsent(toZone, ignored -> new EnumMap<>(ZoneId.class)).putIfAbsent(fromZone, distance);
                }
            }
        }
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
