package com.example.birdnest_back.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

@Data
public class Report {
    private DeviceInformation deviceInformation;

    private List<Drone> capture;
}
