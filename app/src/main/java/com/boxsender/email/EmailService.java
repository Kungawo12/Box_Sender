package com.boxsender.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service            // service component
public class EmailService {

    private final JavaMailSender mailSender;
    
    // Inject configuration from application.properties
    @Value("${brevo.from.email}")
    private String fromEmail;  // 

    // Spring Boot auto-configures JavaMailSender
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send package arrival notification to recipient
     */
    public void sendPackageNotification(String recipientEmail, String recipientName,
                                    String trackingNumber) {
        try {
            //create email message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            //Set email headers
            helper.setFrom(fromEmail, "Box Sender Mailroom");
            helper.setTo(recipientEmail);
            helper.setSubject("Your Package Has Arrived!" + trackingNumber);
            
            String htmlContent = buildEmailTemplate(recipientName, trackingNumber);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println(" Email sent to " + recipientEmail + " from " + fromEmail);
            
        } catch (Exception e) {
            System.err.println(" Email error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Build HTMl email body
    private String buildEmailTemplate(String name, String tracking) {
        String escapedName = escapeHtml(name);
        String escapedTracking = escapeHtml(tracking);
        
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }\n" +
                "        .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }\n" +
                "        .header { background: linear-gradient(135deg, #0a0f1e, #1a2747); color: white; padding: 40px 20px; text-align: center; }\n" +
                "        .header h1 { margin: 0; font-size: 28px; }\n" +
                "        .content { padding: 30px 20px; }\n" +
                "        .tracking-box { background: linear-gradient(135deg, #007bff, #0056b3); color: white; padding: 20px; text-align: center; font-size: 24px; font-weight: bold; margin: 25px 0; border-radius: 8px; letter-spacing: 1px; }\n" +
                "        .instructions { background: #e8f4f8; padding: 15px; border-radius: 4px; margin: 20px 0; }\n" +
                "        .footer { background: #f9fafb; text-align: center; color: #888; font-size: 12px; padding: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\"><h1> Package Arrived!</h1></div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Hi <strong>" + escapedName + "</strong>,</p>\n" +
                "            <p>Your package has been received at our mailroom and is ready for pickup!</p>\n" +
                "            <p><strong>Your Tracking Number:</strong></p>\n" +
                "            <div class=\"tracking-box\">" + escapedTracking + "</div>\n" +
                "            <div class=\"instructions\">\n" +
                "                <h3 style=\"margin-top: 0;\">How to Pick Up:</h3>\n" +
                "                <ol>\n" +
                "                    <li>Go to the mailroom reception</li>\n" +
                "                    <li>Show them your tracking number</li>\n" +
                "                    <li>They'll verify and hand over your package</li>\n" +
                "                </ol>\n" +
                "            </div>\n" +
                "            <p style=\"color: #999; font-size: 13px; font-style: italic;\">\n" +
                "                If you have any questions, please contact the mailroom team.\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p><strong>Box Sender</strong> © 2025</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}