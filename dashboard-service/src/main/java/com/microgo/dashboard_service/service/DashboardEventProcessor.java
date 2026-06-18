package com.microgo.dashboard_service.service;

public interface DashboardEventProcessor {

    boolean process(String message);
}
