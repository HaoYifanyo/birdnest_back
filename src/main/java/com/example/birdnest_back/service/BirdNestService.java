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
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.time.LocalDateTime;
import java.util.*;

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

            Map<String, Object> resultMap = buildDynamoDbClient();
            DynamoDbEnhancedClient enhancedClient = (DynamoDbEnhancedClient) resultMap.get("enhancedClient");
            DynamoDbClient ddb = (DynamoDbClient) resultMap.get("ddb");

            for (Drone drone : dronesViolatedNDZ) {
                Pilot pilot = getPilot(drone.getSerialNumber(), drone.getReportTime());
                putRecord(enhancedClient, pilot) ;
//                System.out.println(pilot.getPilotId()+drone.getSerialNumber());
            }
            ddb.close();
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

        // set report time
        LocalDateTime now = LocalDateTime.now();
        List<Drone> droneList = report.getCapture();
        for (Drone drone : droneList) {
            drone.setReportTime(now);
        }
        return droneList;
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

    public Pilot getPilot(String serialNumber, LocalDateTime reportTime){
        ResponseEntity responseEntity = doGet(pilotUrl + serialNumber);
        HttpStatus statusCode = responseEntity.getStatusCode();
        if(statusCode.equals(HttpStatus.NOT_FOUND)){
            return null;
        }

        String json = (String) responseEntity.getBody();
        Pilot pilot = JSONObject.parseObject(json, Pilot.class);
        pilot.setReportTime(reportTime);
        pilot.setExpireTime(reportTime.plusMinutes(10));

        return pilot;
    }


    public ResponseEntity doGet(String url) {
        ResponseEntity responseEntity = restTemplate.getForEntity(url, String.class);
//        String response = (String) responseEntity.getBody();
        return responseEntity;
    }


    public Map<String, Object> buildDynamoDbClient(){
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
        Region region = Region.US_EAST_1;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ddb", ddb);
        resultMap.put("enhancedClient", enhancedClient);
        return resultMap;
    }
    public static void putRecord(DynamoDbEnhancedClient enhancedClient, Pilot pilot) {
        try {
            DynamoDbTable<Pilot> pilotTable = enhancedClient.table("pilot", TableSchema.fromBean(Pilot.class));

            // Put the pilot data into an Amazon DynamoDB table.
            pilotTable.putItem(pilot);

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        // todo
        System.out.println("Customer data added to the table with id id101");
    }



}
