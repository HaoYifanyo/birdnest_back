package com.example.birdnest_back.service;

import com.alibaba.fastjson.JSONObject;
import com.example.birdnest_back.entity.Drone;
import com.example.birdnest_back.entity.Pilot;
import com.example.birdnest_back.entity.Report;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BirdNestService {
    @Autowired
    private RestTemplate restTemplate;

    private static final String dronesUrl = "https://assignments.reaktor.com/birdnest/drones";
    private static final String pilotUrl = "https://assignments.reaktor.com/birdnest/pilots/";

    // Run every 2 seconds
    @Scheduled(cron = "*/2 * * * * ?")
    public void scheduledTask(){
        List<Drone> allDroneList = getDrones();
        List<Drone> dronesViolatedNDZ = getDronesViolatedNDZ(allDroneList);
        if(!dronesViolatedNDZ.isEmpty()){
            System.out.println(dronesViolatedNDZ);
        }

    }

    public List<Drone> getDrones(){
        String xml = (String) doGet(dronesUrl).getBody();

        // convert xml to java object
        XStream x = new XStream(new StaxDriver());
        x.addPermission(AnyTypePermission.ANY);
        x.alias("report", Report.class);
        x.alias("drone", Drone.class);
        x.alias("capture", List.class);
        Report report = (Report) x.fromXML(xml);

        return report.getCapture();
    }

    public List<Drone> getDronesViolatedNDZ(List<Drone> droneList){
        List<Drone> dronesViolatedNDZ = new ArrayList<>();
        Double originX = 250000d;
        Double originY = 250000d;
        Double NDZDistance = 100000d;
        for (Drone drone : droneList) {
            Double droneX = drone.getPositionX();
            Double droneY = drone.getPositionY();
            Double distance = Math.sqrt((droneX - originX) * (droneX - originX) + (droneY - originY) * (droneY - originY));
            if(distance.compareTo(NDZDistance) == -1){
                drone.setDistance(distance/1000);
                dronesViolatedNDZ.add(drone);
            }
        }
        return dronesViolatedNDZ;
    }

    public Pilot getPilot(String serialNumber){
        ResponseEntity responseEntity = doGet(pilotUrl + serialNumber);
        HttpStatus statusCode = responseEntity.getStatusCode();
        if(statusCode.equals(HttpStatus.NOT_FOUND)){
            return null;
        }

        String json = (String) responseEntity.getBody();
        Pilot pilot = JSONObject.parseObject(json, Pilot.class);

        return pilot;
    }


    public ResponseEntity doGet(String url) {
        ResponseEntity responseEntity = restTemplate.getForEntity(url, String.class);
//        String response = (String) responseEntity.getBody();
        return responseEntity;
    }

}
