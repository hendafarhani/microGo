package com.microgo.optimization_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = OptimizationController.class)
public class OptimizationExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleUnavailableOptimizationState(IllegalStateException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());
        problem.setTitle("Optimization state unavailable");
        return problem;
    }
}
