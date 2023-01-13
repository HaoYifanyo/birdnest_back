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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BirdNestService {
    @Autowired
    private RestTemplate restTemplate;

    private static final String dronesUrl = "https://assignments.reaktor.com/birdnest/drones";
    private static final String pilotUrl = "https://assignments.reaktor.com/birdnest/pilots/";

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
