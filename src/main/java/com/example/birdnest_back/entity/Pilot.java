package com.example.birdnest_back.entity;

import lombok.Data;

@Data
public class Pilot {
    private String pilotId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String createdDt;
    private String email;

}
