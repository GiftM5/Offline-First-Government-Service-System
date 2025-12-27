package com.gov.centralserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @Autowired
    private ApplicationRepository repo;

    @JmsListener(destination = "application.sync.queue")
    public void receiveMessage(String message) {
        // Assume message is "citizenId,serviceType"
        String[] parts = message.split(",");
        if (parts.length == 2) {
            Application app = new Application();
            app.setCitizenId(parts[0]);
            app.setServiceType(parts[1]);
            app.setSynced(true);
            repo.save(app);
            System.out.println("Application synced: " + app.getCitizenId());
        }
    }

}