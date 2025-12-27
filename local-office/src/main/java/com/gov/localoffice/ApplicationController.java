package com.gov.localoffice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplicationController {

    @Autowired
    private ApplicationRepository repo;

    @Autowired
    private JmsTemplate jmsTemplate;

    @GetMapping("/")
    public String home() {
        return "application-form";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam String citizenId, @RequestParam String serviceType) {
        Application app = new Application();
        app.setCitizenId(citizenId);
        app.setServiceType(serviceType);
        repo.save(app);
        
        // Send message to queue
        jmsTemplate.convertAndSend("application.sync.queue", citizenId + "," + serviceType);
        return "redirect:/status";
    }

    @GetMapping("/status")
    public String status(Model model) {
        model.addAttribute("applications", repo.findAll());
        return "status";
    }

}