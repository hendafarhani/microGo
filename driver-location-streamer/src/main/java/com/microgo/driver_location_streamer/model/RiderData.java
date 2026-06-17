package com.microgo.driver_location_streamer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderData implements Serializable {
    private String identifier;
    private String userName;
    private Location location;
}
