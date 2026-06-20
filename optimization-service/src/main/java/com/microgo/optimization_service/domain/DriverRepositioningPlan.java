package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DriverRepositioningPlan {
    private DriverPlanningEntity driver;
    private ZoneId targetZone;
    private double priorityScore;
    private int expectedWaitTimeReductionSeconds;
    private double expectedCancellationReduction;

    public boolean isMoveRecommended() {
        return targetZone != null && targetZone != driver.getCurrentZone();
    }
}
