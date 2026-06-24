create table if not exists optimization_run (
    id uuid primary key,
    simulation_run_id uuid not null,
    scenario varchar(64) not null,
    trigger_source varchar(64) not null,
    solver_status varchar(64) not null,
    snapshot_generated_at timestamp with time zone not null,
    started_at timestamp with time zone not null,
    completed_at timestamp with time zone,
    baseline_strategy varchar(64) not null,
    optimization_strategy varchar(64) not null,
    score_summary text
);

create table if not exists optimization_result (
    id uuid primary key,
    optimization_run_id uuid not null references optimization_run(id),
    driver_count integer not null,
    pending_ride_count integer not null,
    demand_summary jsonb not null,
    baseline_metrics jsonb not null,
    optimized_metrics jsonb not null,
    score_hard integer,
    score_soft integer,
    created_at timestamp with time zone not null
);

create table if not exists baseline_vs_optimized_metrics (
    id uuid primary key,
    optimization_run_id uuid not null references optimization_run(id),
    metric_name varchar(128) not null,
    baseline_value numeric(12,4) not null,
    optimized_value numeric(12,4) not null,
    improvement numeric(12,4) not null,
    unit varchar(32) not null
);

create table if not exists driver_repositioning_recommendation (
    id uuid primary key,
    optimization_run_id uuid not null references optimization_run(id),
    driver_id varchar(128) not null,
    current_zone varchar(64) not null,
    target_zone varchar(64) not null,
    distance_km numeric(8,3) not null,
    priority_score numeric(8,4) not null,
    expected_wait_reduction_seconds integer,
    expected_cancellation_reduction numeric(8,4),
    recommendation_status varchar(64) not null,
    created_at timestamp with time zone not null
);

create index if not exists idx_optimization_run_simulation_run
    on optimization_run(simulation_run_id);

create index if not exists idx_driver_repositioning_recommendation_driver
    on driver_repositioning_recommendation(driver_id);
