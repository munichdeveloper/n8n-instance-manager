package de.dgtlschmd.controla.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${mail.from:noreply@controla.local}")
    private String mailFrom;

    @Value("${mail.from-name:controla}")
    private String mailFromName;

    @Value("${app.email.locale:de_DE}")
    private String emailLocale;

    @Value("${app.email.branding:controla}")
    private String emailBranding;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sendet eine Passwort-Reset-E-Mail
     */
    public void sendPasswordResetEmail(String toEmail, String username, String resetUrl) {
        try {
            String subject = getLocalizedSubject();
            String htmlContent = loadEmailTemplate(username, resetUrl);

            sendHtmlEmail(toEmail, subject, htmlContent);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Fehler beim Senden der E-Mail", e);
        }
    }

    /**
     * Lädt das E-Mail-Template und ersetzt Platzhalter
     */
    private String loadEmailTemplate(String username, String resetUrl) {
        String templatePath = String.format("email-templates/%s/%s/password-reset/email.html",
                emailLocale, emailBranding);

        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Platzhalter ersetzen
            template = template.replace("{{username}}", username);
            template = template.replace("{{resetUrl}}", resetUrl);

            return template;
        } catch (IOException e) {
            log.warn("Template not found: {}, using fallback", templatePath);
            return getFallbackTemplate(username, resetUrl);
        }
    }

    /**
     * Fallback-Template, falls keine spezifische Vorlage gefunden wird
     */
    private String getFallbackTemplate(String username, String resetUrl) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset=\"UTF-8\"></head><body>" +
                "<h2>Passwort zurücksetzen</h2>" +
                "<p>Hallo " + username + ",</p>" +
                "<p>Sie haben eine Anfrage zum Zurücksetzen Ihres Passworts gestellt.</p>" +
                "<p>Klicken Sie auf den folgenden Link, um Ihr Passwort zurückzusetzen:</p>" +
                "<p><a href=\"" + resetUrl + "\">" + resetUrl + "</a></p>" +
                "<p>Dieser Link ist 24 Stunden gültig.</p>" +
                "<p>Falls Sie diese Anfrage nicht gestellt haben, können Sie diese E-Mail ignorieren.</p>" +
                "<p>Mit freundlichen Grüßen,<br>Ihr controla Team</p>" +
                "</body></html>";
    }

    /**
     * Gibt den lokalisierten Betreff zurück
     */
    private String getLocalizedSubject() {
        if (emailLocale.startsWith("en")) {
            return "Password Reset Request";
        }
        return "Passwort zurücksetzen";
    }

    /**
     * Sendet eine HTML-E-Mail
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailFromName + " <" + mailFrom + ">");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}

