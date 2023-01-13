package com.example.birdnest_back;

import com.example.birdnest_back.entity.Drone;
import com.example.birdnest_back.service.BirdNestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class BirdnestBackApplicationTests {

    @Autowired
    BirdNestService birdNestService;
    @Test
    void contextLoads() {
    }

    @Test
    void testGet() {
        List<Drone> droneList = birdNestService.getDrones();
        birdNestService.getPilot(droneList.get(0).getSerialNumber());

    }

}
