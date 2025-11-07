package com.boxsender.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boxsender.packages.Package;
import com.boxsender.packages.PackageRepository;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final PackageRepository packageRepo;

    public ActivityController(PackageRepository packageRepo) {
        this.packageRepo = packageRepo;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
        @RequestParam(value = "limit", required = false, defaultValue = "100") int limit
    ) {
        List<Package> packages = packageRepo.findAll();
        packages.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        if (packages.size() > limit) {
            packages = packages.subList(0, limit);
        }
        
        List<Map<String, Object>> activities = new ArrayList<>();
        
        for (Package pkg : packages) {
            Map<String, Object> receivedEvent = new HashMap<>();
            receivedEvent.put("action", "RECEIVED");
            receivedEvent.put("when", pkg.getCreatedAt().toString());
            receivedEvent.put("trackingNumber", pkg.getTrackingNumber());
            receivedEvent.put("recipient", pkg.getRecipient().getFirstName() + " " + pkg.getRecipient().getLastName());
            
            String receivedDetails = "Carrier: " + pkg.getCarrier();
            if (pkg.getDescription() != null && !pkg.getDescription().isEmpty()) {
                receivedDetails += " - " + pkg.getDescription();
            }
            receivedEvent.put("details", receivedDetails);
            activities.add(receivedEvent);
            
            if ("picked_up".equals(pkg.getStatus()) && pkg.getPickedUpAt() != null) {
                Map<String, Object> pickedUpEvent = new HashMap<>();
                pickedUpEvent.put("action", "PICKED_UP");
                pickedUpEvent.put("when", pkg.getPickedUpAt().toString());
                pickedUpEvent.put("trackingNumber", pkg.getTrackingNumber());
                pickedUpEvent.put("recipient", pkg.getRecipient().getFirstName() + " " + pkg.getRecipient().getLastName());
                pickedUpEvent.put("details", "Picked up by: " + pkg.getPickedUpBy());
                activities.add(pickedUpEvent);
            }
        }
        
        activities.sort((a, b) -> {
            String timeA = (String) a.get("when");
            String timeB = (String) b.get("when");
            return timeB.compareTo(timeA);
        });
        
        if (activities.size() > limit) {
            activities = activities.subList(0, limit);
        }
        
        return ResponseEntity.ok(activities);
    }
}
