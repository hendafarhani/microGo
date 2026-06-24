package com.microgo.simulation_service.service;

/**
 * Owns the dispatch-availability view of simulated drivers. Availability is a decision/lifecycle
 * concern (accept makes a driver busy; a completed or cancelled ride frees it), so the simulation
 * service maintains it — the generator only owns movement and positions.
 *
 * <p>Backed by a Redis SET of available driver ids (no positions; those live in the generator's
 * {@code vehicle_location} geo set). Dispatch matching in ride-request intersects the nearest
 * vehicles with this set.
 */
public interface DriverAvailabilityRegistry {

    /** Marks a driver as available for dispatch (newly generated, freed after a trip or cancellation). */
    void markAvailable(String driverId);

    /** Marks a driver as unavailable for dispatch (it accepted and is now serving a ride). */
    void markBusy(String driverId);
}
