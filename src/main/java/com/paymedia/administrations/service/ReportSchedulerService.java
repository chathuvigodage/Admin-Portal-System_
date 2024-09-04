package com.paymedia.administrations.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReportSchedulerService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${report.download.base.url}")
    private String reportDownloadBaseUrl;

    @Autowired
    private UserService userService;

    // Schedule the email to be sent every day at 12:00 PM
    @Scheduled(cron = "0 0 12 * * ?")
    public void sendDailyReportSummary() {
        try {
            // Generate the report link
            String userReportLink = generateUserReportDownloadLink();
            String roleReportLink = generateRoleReportDownloadLink();


            // Send the email with the report link
            sendEmailWithReportLink(userReportLink,roleReportLink);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send report email", e);
        }
    }

    private String generateUserReportDownloadLink() {
        // Example date range for the report (you can customize this)
        String fromDateStr = "2024-09-01T00:00:00"; // Start date
        String toDateStr = "2024-09-04T23:59:59"; // End date
        String reportType = "pdf"; // Report format (pdf, xlsx, csv)

        // Construct the download URL
        return reportDownloadBaseUrl + "users/report?fromDate=" + fromDateStr + "&toDate=" + toDateStr + "&reportType=" + reportType;
    }

    private String generateRoleReportDownloadLink() {
        // Example date range for the report (you can customize this)
        String fromDateStr = "2024-09-01T00:00:00"; // Start date
        String toDateStr = "2024-09-04T23:59:59"; // End date
        String reportType = "pdf"; // Report format (pdf, xlsx, csv)

        // Construct the download URL
        return reportDownloadBaseUrl + "roles/report?fromDate=" + fromDateStr + "&toDate=" + toDateStr + "&reportType=" + reportType;
    }


    private void sendEmailWithReportLink(String userReportLink, String roleReportLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("chathuvigodage02@gmail.com");  // Set the sender's email address
        helper.setTo("chathuvigodage08@gmail.com");
        helper.setSubject("Daily Report Summary");
        helper.setText("You can download the daily user report using the following link:\n\n" + userReportLink + "\n\n" +
                "You can download the daily role report using the following link:\n\n" + roleReportLink);

        mailSender.send(message);
    }
}


