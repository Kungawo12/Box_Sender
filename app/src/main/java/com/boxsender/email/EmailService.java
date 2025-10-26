package com.boxsender.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send HTML email notification to recipient about their package
     */
    public void sendPackageNotification(String recipientEmail, String recipientName,
                                       String trackingNumber, String pickupCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setSubject("📦 Your Package Has Arrived!");
            
            String htmlContent = buildEmailTemplate(recipientName, trackingNumber, pickupCode);
            helper.setText(htmlContent, true);  // true = HTML content

            mailSender.send(message);
            System.out.println("✅ Email sent to " + recipientEmail);
            
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email to " + recipientEmail + ": " + e.getMessage());
            // Don't throw exception - graceful failure
            // Package was already saved, don't break the flow due to email failure
        }
    }

    /**
     * Build HTML email template
     */
    private String buildEmailTemplate(String name, String tracking, String code) {
        // Escape the variables for security
        String escapedName = escapeHtml(name);
        String escapedTracking = escapeHtml(tracking);
        String escapedCode = escapeHtml(code);
        
        // Build HTML string without mixing """ with +
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <style>\n" +
                "        body { \n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "            background-color: #f0f2f5;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container { \n" +
                "            max-width: 600px;\n" +
                "            margin: 20px auto;\n" +
                "            background: white;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 2px 8px rgba(0,0,0,0.1);\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "        .header { \n" +
                "            background: linear-gradient(135deg, #0a0f1e, #1a2747);\n" +
                "            color: white;\n" +
                "            padding: 40px 20px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .header h1 {\n" +
                "            margin: 0;\n" +
                "            font-size: 28px;\n" +
                "        }\n" +
                "        .content { \n" +
                "            padding: 30px 20px;\n" +
                "        }\n" +
                "        .greeting {\n" +
                "            font-size: 16px;\n" +
                "            color: #333;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .info-section {\n" +
                "            background: #f9fafb;\n" +
                "            border-left: 4px solid #007bff;\n" +
                "            padding: 15px;\n" +
                "            margin: 20px 0;\n" +
                "            border-radius: 4px;\n" +
                "        }\n" +
                "        .info-section h3 {\n" +
                "            margin-top: 0;\n" +
                "            color: #0a0f1e;\n" +
                "            font-size: 14px;\n" +
                "            text-transform: uppercase;\n" +
                "            letter-spacing: 0.5px;\n" +
                "        }\n" +
                "        .info-item {\n" +
                "            margin: 10px 0;\n" +
                "            font-size: 14px;\n" +
                "            color: #555;\n" +
                "        }\n" +
                "        .info-label {\n" +
                "            font-weight: bold;\n" +
                "            color: #0a0f1e;\n" +
                "        }\n" +
                "        .code-box { \n" +
                "            background: linear-gradient(135deg, #007bff, #0056b3);\n" +
                "            border: 2px solid #0056b3;\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "            font-size: 32px;\n" +
                "            font-weight: bold;\n" +
                "            color: white;\n" +
                "            margin: 25px 0;\n" +
                "            border-radius: 8px;\n" +
                "            letter-spacing: 2px;\n" +
                "            font-family: 'Courier New', monospace;\n" +
                "        }\n" +
                "        .instructions {\n" +
                "            background: #e8f4f8;\n" +
                "            padding: 15px;\n" +
                "            border-radius: 4px;\n" +
                "            margin: 20px 0;\n" +
                "        }\n" +
                "        .instructions ol {\n" +
                "            margin: 10px 0;\n" +
                "            padding-left: 20px;\n" +
                "        }\n" +
                "        .instructions li {\n" +
                "            margin: 8px 0;\n" +
                "            color: #333;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "        .footer { \n" +
                "            background: #f9fafb;\n" +
                "            text-align: center;\n" +
                "            color: #888;\n" +
                "            font-size: 12px;\n" +
                "            padding: 20px;\n" +
                "            border-top: 1px solid #e5e7eb;\n" +
                "        }\n" +
                "        .footer p {\n" +
                "            margin: 5px 0;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>📦 Package Arrived!</h1>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"content\">\n" +
                "            <div class=\"greeting\">\n" +
                "                Hi <strong>" + escapedName + "</strong>,\n" +
                "            </div>\n" +
                "            \n" +
                "            <p style=\"color: #666; font-size: 15px; line-height: 1.6;\">\n" +
                "                Your package has been received at our mailroom and is ready for pickup!\n" +
                "            </p>\n" +
                "            \n" +
                "            <div class=\"info-section\">\n" +
                "                <h3>📋 Pickup Details</h3>\n" +
                "                <div class=\"info-item\">\n" +
                "                    <span class=\"info-label\">Tracking Number:</span> " + escapedTracking + "\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p style=\"color: #666; font-size: 14px; margin: 20px 0;\">\n" +
                "                <strong>Your Pickup Code:</strong>\n" +
                "            </p>\n" +
                "            \n" +
                "            <div class=\"code-box\">" + escapedCode + "</div>\n" +
                "            \n" +
                "            <div class=\"instructions\">\n" +
                "                <h3 style=\"margin-top: 0; color: #0a0f1e; font-size: 14px;\">How to Pick Up:</h3>\n" +
                "                <ol>\n" +
                "                    <li>Go to the mailroom reception</li>\n" +
                "                    <li>Show them or tell them this pickup code</li>\n" +
                "                    <li>They'll verify and hand over your package</li>\n" +
                "                </ol>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p style=\"color: #999; font-size: 13px; font-style: italic;\">\n" +
                "                If you have any questions or didn't expect this package, \n" +
                "                please contact the mailroom team immediately.\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"footer\">\n" +
                "            <p><strong>Box Sender</strong> © 2025</p>\n" +
                "            <p>Fast Package Logging & Pickups</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Escape HTML special characters for security
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
}