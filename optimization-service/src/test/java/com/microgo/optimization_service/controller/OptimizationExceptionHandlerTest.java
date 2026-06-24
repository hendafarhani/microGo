package com.microgo.optimization_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizationExceptionHandlerTest {

    @Test
    void unavailableStateIsReportedAsConflict() {
        ProblemDetail problem = new OptimizationExceptionHandler()
                .handleUnavailableOptimizationState(new IllegalStateException("state missing"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Optimization state unavailable");
        assertThat(problem.getDetail()).isEqualTo("state missing");
    }
}
