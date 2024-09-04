package com.paymedia.administrations.controller;

import com.paymedia.administrations.service.ReportSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private ReportSchedulerService reportSchedulerService;

    @GetMapping("/send-report")
    public ResponseEntity<String> sendReportEmail() {
        try {
            reportSchedulerService.sendDailyReportSummary();
            return ResponseEntity.ok("Email sent successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }
}

