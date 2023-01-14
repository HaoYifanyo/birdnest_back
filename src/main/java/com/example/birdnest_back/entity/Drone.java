package com.example.birdnest_back.entity;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import lombok.Data;

@Data
public class Drone {
    private String serialNumber;
    private String model;
    private String manufacturer;
    private String mac;
    private String ipv4;
    private String ipv6;
    private String firmware;
    private Double positionY;
    private Double positionX;
    private Double altitude;

    // distance to the nest
    @XStreamOmitField
    private Double distance;
}
