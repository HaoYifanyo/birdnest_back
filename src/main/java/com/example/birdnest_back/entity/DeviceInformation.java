package com.example.birdnest_back.entity;

import lombok.Data;

@Data
public class DeviceInformation {
    private Double listenRange;
    private String deviceStarted;
    private Double uptimeSeconds;
    private Double updateIntervalMs;

}
