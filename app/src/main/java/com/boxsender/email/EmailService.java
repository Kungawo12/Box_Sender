package com.boxsender.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Email Service
 *
 * This service handles sending email notifications to package recipients.
 * It uses Spring's JavaMailSender to send HTML-formatted emails via SMTP.
 *
 * Email Provider: Brevo (formerly Sendinblue)
 * - SMTP service configured in application.properties
 * - Requires verified sender email address
 * - Used for transactional emails (package notifications)
 *
 * Key Features:
 * - Sends HTML emails with professional formatting
 * - Includes tracking number prominently
 * - Provides pickup instructions
 * - Escapes HTML to prevent injection attacks
 * - Handles errors gracefully (doesn't fail package logging)
 *
 * Email Flow:
 * 1. Package is logged in PackageController
 * 2. This service is called to notify recipient
 * 3. HTML email is generated with tracking details
 * 4. Email sent via Brevo SMTP server
 * 5. Recipient receives notification with pickup instructions
 */
@Service  // Marks this as a Spring service component
public class EmailService {

    // JavaMailSender is Spring's interface for sending emails
    // Automatically configured by Spring Boot based on application.properties
    private final JavaMailSender mailSender;

    // Reads sender email from application.properties
    // @Value annotation injects property value from config file
    // Property key: brevo.from.email
    @Value("${brevo.from.email}")
    private String fromEmail;  // Must be verified in Brevo account

    /**
     * Constructor-based Dependency Injection
     *
     * @param mailSender JavaMailSender configured by Spring Boot
     */
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send Package Arrival Notification Email with Pickup Code
     *
     * Sends a professionally formatted HTML email to notify a recipient
     * that their package has arrived and is ready for pickup.
     *
     * Email includes:
     * - Personalized greeting with recipient's name
     * - Tracking number prominently displayed
     * - Secure 6-character pickup verification code
     * - Step-by-step pickup instructions
     * - Professional styling with gradients and boxes
     *
     * Error Handling:
     * - Catches all exceptions to prevent package logging failure
     * - Logs error details to console for debugging
     * - Returns gracefully even if email fails
     *
     * @param recipientEmail the email address to send notification to
     * @param recipientName the recipient's first name for personalization
     * @param trackingNumber the tracking number for the package
     * @param pickupCode the 6-character verification code for pickup
     */
    public void sendPackageNotification(String recipientEmail, String recipientName,
                                    String trackingNumber, String pickupCode) {
        try {
            // Step 1: Create MIME message (supports HTML and attachments)
            MimeMessage message = mailSender.createMimeMessage();

            // Step 2: Use helper to set message properties easily
            // 'true' parameter enables multipart mode (for HTML and attachments)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Step 3: Set email headers
            // Use verified Brevo SMTP email as sender
            helper.setFrom(fromEmail, "Box Sender Mailroom");
            helper.setTo(recipientEmail);
            helper.setSubject("Your Package Has Arrived!");

            // Step 4: Build HTML email content with template
            String htmlContent = buildEmailTemplate(recipientName, trackingNumber, pickupCode);

            // Step 5: Set email body (true = HTML format, not plain text)
            helper.setText(htmlContent, true);

            // Step 6: Send the email via SMTP
            mailSender.send(message);

            // Step 7: Log success for debugging
            System.out.println("✓ Email sent to " + recipientEmail + " from " + fromEmail);

        } catch (Exception e) {
            // Catch any email errors (SMTP failures, connection issues, etc.)
            // Don't throw exception - email failure shouldn't fail package logging
            System.err.println("✗ Email error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build HTML Email Template with Pickup Code
     *
     * Generates a professionally styled HTML email with the recipient's information
     * including a secure pickup verification code.
     *
     * Template Features:
     * - Responsive design (max-width: 600px for email clients)
     * - Professional color scheme with gradients
     * - Prominent tracking number display
     * - Large, easy-to-read pickup code
     * - Step-by-step pickup instructions
     * - Proper HTML structure with meta tags and styles
     *
     * Security Note:
     * - Escapes user input (name and tracking) to prevent HTML injection
     * - Without escaping, malicious input like "<script>alert('XSS')</script>"
     *   could execute JavaScript in recipient's email client
     *
     * @param name recipient's name (will be HTML-escaped)
     * @param tracking package tracking number (will be HTML-escaped)
     * @param pickupCode 6-character pickup verification code
     * @return complete HTML email as a string
     */
    private String buildEmailTemplate(String name, String tracking, String pickupCode) {
        // Escape HTML special characters to prevent injection attacks
        String escapedName = escapeHtml(name);
        String escapedTracking = escapeHtml(tracking);
        String escapedCode = escapeHtml(pickupCode);

        // Build HTML email using string concatenation
        // In production, consider using a templating engine like Thymeleaf
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        /* Email-friendly CSS - inline styles preferred for email clients */\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }\n" +
                "        .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }\n" +
                "        .header { background: linear-gradient(135deg, #0a0f1e, #1a2747); color: white; padding: 40px 20px; text-align: center; }\n" +
                "        .header h1 { margin: 0; font-size: 28px; }\n" +
                "        .content { padding: 30px 20px; }\n" +
                "        .tracking-box { background: linear-gradient(135deg, #007bff, #0056b3); color: white; padding: 20px; text-align: center; font-size: 24px; font-weight: bold; margin: 25px 0; border-radius: 8px; letter-spacing: 1px; }\n" +
                "        .code-box { background: linear-gradient(135deg, #28a745, #1e7e34); color: white; padding: 25px; text-align: center; font-size: 36px; font-weight: bold; margin: 25px 0; border-radius: 8px; letter-spacing: 4px; font-family: 'Courier New', monospace; }\n" +
                "        .code-label { font-size: 14px; font-weight: normal; letter-spacing: normal; margin-bottom: 10px; opacity: 0.9; }\n" +
                "        .instructions { background: #e8f4f8; padding: 15px; border-radius: 4px; margin: 20px 0; }\n" +
                "        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin: 20px 0; color: #856404; }\n" +
                "        .footer { background: #f9fafb; text-align: center; color: #888; font-size: 12px; padding: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\"><h1>📦 Package Arrived!</h1></div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Hi <strong>" + escapedName + "</strong>,</p>\n" +
                "            <p>Your package has been received at our mailroom and is ready for pickup!</p>\n" +
                "            <p><strong>Tracking Number:</strong></p>\n" +
                "            <div class=\"tracking-box\">" + escapedTracking + "</div>\n" +
                "            <div class=\"code-box\">\n" +
                "                <div class=\"code-label\">YOUR PICKUP CODE</div>\n" +
                "                <div>" + escapedCode + "</div>\n" +
                "            </div>\n" +
                "            <div class=\"warning\">\n" +
                "                <strong>⚠️ Important:</strong> You will need this pickup code to collect your package. Please keep this email or write down the code.\n" +
                "            </div>\n" +
                "            <div class=\"instructions\">\n" +
                "                <h3 style=\"margin-top: 0;\">📋 How to Pick Up:</h3>\n" +
                "                <ol>\n" +
                "                    <li>Go to the mailroom reception</li>\n" +
                "                    <li>Provide your <strong>tracking number</strong> and <strong>pickup code</strong></li>\n" +
                "                    <li>Show a valid ID for verification</li>\n" +
                "                    <li>Sign for your package and receive it</li>\n" +
                "                </ol>\n" +
                "            </div>\n" +
                "            <p style=\"color: #999; font-size: 13px; font-style: italic;\">\n" +
                "                If you have any questions or didn't order this package, please contact the mailroom team immediately.\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p><strong>Box Sender</strong> © 2025</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Escape HTML Special Characters
     *
     * Prevents HTML/JavaScript injection attacks by converting special characters
     * to their HTML entity equivalents.
     *
     * Security Conversions:
     * - & becomes &amp; (must be first to not double-escape)
     * - < becomes &lt; (prevents opening tags)
     * - > becomes &gt; (prevents closing tags)
     *
     * Example Attack Prevention:
     * Input: "<script>alert('XSS')</script>"
     * Output: "&lt;script&gt;alert('XSS')&lt;/script&gt;"
     * Result: Displays as text instead of executing JavaScript
     *
     * Why This Matters:
     * - User input (name, tracking) comes from frontend forms
     * - Without escaping, malicious users could inject HTML/JavaScript
     * - This would affect the recipient's email, not the attacker
     * - XSS (Cross-Site Scripting) vulnerability if not escaped
     *
     * @param text the text to escape (can be null)
     * @return escaped text safe for HTML, or empty string if input is null
     */
    private String escapeHtml(String text) {
        // Handle null input gracefully
        if (text == null) return "";

        // Replace special characters with HTML entities
        // Order matters: & must be replaced first to avoid double-escaping
        return text.replace("&", "&amp;")   // Ampersand first
                   .replace("<", "&lt;")    // Less than (opening tag)
                   .replace(">", "&gt;");   // Greater than (closing tag)
    }
}