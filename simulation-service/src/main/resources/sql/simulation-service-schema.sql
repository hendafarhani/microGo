create table if not exists scenario_config (
    id uuid primary key,
    scenario_name varchar(64) not null unique,
    city varchar(64) not null,
    anchor_zone varchar(64) not null,
    start_time timestamp with time zone not null,
    weather varchar(32) not null,
    traffic_level varchar(32) not null,
    passenger_demand_multiplier numeric(8,4) not null,
    cancellation_risk_multiplier numeric(8,4) not null,
    driver_speed_multiplier numeric(8,4) not null,
    airport_fare_bias numeric(8,4) not null default 1.0,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table if not exists simulation_run (
    id uuid primary key,
    scenario_config_id uuid not null references scenario_config(id),
    status varchar(32) not null,
    started_at timestamp with time zone not null,
    completed_at timestamp with time zone,
    requested_by varchar(128),
    notes varchar(512)
);

create table if not exists passenger_profile (
    id uuid primary key,
    simulation_run_id uuid not null references simulation_run(id),
    external_passenger_id varchar(64) not null unique,
    origin_zone varchar(64) not null,
    destination_zone varchar(64) not null,
    urgency_score numeric(8,4) not null,
    cancellation_sensitivity numeric(8,4) not null,
    created_at timestamp with time zone not null
);

create table if not exists driver_profile (
    id uuid primary key,
    simulation_run_id uuid not null references simulation_run(id),
    external_driver_id varchar(64) not null unique,
    home_zone varchar(64) not null,
    fatigue_score numeric(8,4) not null,
    airport_preference numeric(8,4) not null,
    destination_bias varchar(64) not null,
    reliability_score numeric(8,4) not null,
    created_at timestamp with time zone not null
);

create table if not exists simulation_metrics (
    id uuid primary key,
    simulation_run_id uuid not null references simulation_run(id),
    pending_ride_requests integer not null,
    accepted_rides integer not null,
    refused_rides integer not null,
    cancelled_rides integer not null,
    average_waiting_time_seconds numeric(10,2) not null,
    cancellation_risk numeric(8,4) not null,
    acceptance_probability numeric(8,4) not null,
    updated_at timestamp with time zone not null
);

create table if not exists simulation_result (
    id uuid primary key,
    simulation_run_id uuid not null unique references simulation_run(id),
    active_scenario varchar(64) not null,
    predicted_demand_by_zone jsonb not null,
    pending_ride_requests integer not null,
    driver_acceptance_probability numeric(8,4) not null,
    average_waiting_time_seconds numeric(10,2) not null,
    cancellation_risk numeric(8,4) not null,
    metrics_snapshot jsonb not null,
    completed_at timestamp with time zone not null
);
