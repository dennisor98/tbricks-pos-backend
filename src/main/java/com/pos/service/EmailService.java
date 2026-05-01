package com.pos.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Slf4j
@Service
public class EmailService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String fromEmail;
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final String verificationBaseUrl;

    public EmailService(@Value("${resend.api.key}") String apiKey,
                        @Value("${resend.from.email}") String fromEmail,
                        @Value("${app.verification.base-url}") String verificationBaseUrl) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.verificationBaseUrl = verificationBaseUrl;
        this.restTemplate = new RestTemplate();
    }

    public void sendVerificationCode(String toEmail, String code) {
        try {
            String htmlContent = buildVerificationEmailHtml(code);

            EmailRequest emailRequest = new EmailRequest(fromEmail, toEmail, "TBRICKS Verification Code", htmlContent);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<EmailRequest> request = new HttpEntity<>(emailRequest, headers);
            EmailResponse response = restTemplate.postForObject(RESEND_API_URL, request, EmailResponse.class);

            log.info("Verification code sent to email: {}, ID: {}", toEmail, response.getId());
        } catch (Exception e) {
            log.error("Failed to send verification code to email: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification code. Please try again.");
        }
    }

    private String buildVerificationEmailHtml(String code) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\"/>\n" +
                "  <style>\n" +
                "    @import url('https://fonts.googleapis.com/css2?family=Space+Mono:wght@400;700&family=Syne:wght@400;700;800&display=swap');\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body style=\"margin:0;padding:0;background-color:#0a0a0f;font-family:'Syne',sans-serif;\">\n" +
                "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#0a0a0f;padding:40px 20px;\">\n" +
                "    <tr><td align=\"center\">\n" +
                "      <table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"\n" +
                "             style=\"max-width:560px;width:100%;border-radius:16px;overflow:hidden;background-color:#13131f;border:1px solid #2a2a3d;\">\n" +
                "        <tr><td style=\"height:4px;background:linear-gradient(90deg,#4f46e5,#7c3aed,#ec4899);font-size:0;\">&nbsp;</td></tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding:40px 48px 32px;background:linear-gradient(160deg,#1a1a2e 0%,#13131f 100%);\">\n" +
                "            <span style=\"font-family:'Syne',sans-serif;font-size:22px;font-weight:800;letter-spacing:0.08em;color:#ffffff;\">T<span style=\"color:#7c3aed;\">&#9632;</span>BRICKS</span>\n" +
                "            <div style=\"height:1px;background:linear-gradient(90deg,#2a2a3d,transparent);margin-top:24px;\"></div>\n" +
                "            <h1 style=\"margin:28px 0 8px;font-family:'Syne',sans-serif;font-size:28px;font-weight:800;color:#ffffff;letter-spacing:-0.01em;\">Verify your identity</h1>\n" +
                "            <p style=\"margin:0;font-size:15px;color:#6b7280;line-height:1.6;\">Use the code below to complete your sign-in. It's only valid for a short time.</p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding:0 48px 36px;\">\n" +
                "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "              <tr>\n" +
                "                <td style=\"background:linear-gradient(135deg,#1e1b4b 0%,#1a1a2e 100%);border:1px solid #3730a3;border-radius:12px;padding:32px;text-align:center;\">\n" +
                "                  <p style=\"margin:0 0 12px;font-family:'Space Mono',monospace;font-size:11px;letter-spacing:0.2em;color:#6366f1;text-transform:uppercase;\">One-Time Code</p>\n" +
                "                  <div style=\"font-family:'Space Mono',monospace;font-size:48px;font-weight:700;letter-spacing:0.25em;color:#ffffff;text-shadow:0 0 32px rgba(124,58,237,0.6);padding:8px 0;\">\n" +
                "                    " + code + "\n" +
                "                  </div>\n" +
                "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" style=\"margin-top:20px;\">\n" +
                "                    <tr><td style=\"background-color:rgba(239,68,68,0.12);border:1px solid rgba(239,68,68,0.25);border-radius:999px;padding:6px 16px;\">\n" +
                "                      <span style=\"font-family:'Space Mono',monospace;font-size:11px;color:#f87171;letter-spacing:0.05em;\">&#9201; Expires in 5 minutes</span>\n" +
                "                    </td></tr>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "            </table>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding:0 48px 36px;\">\n" +
                "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "              <tr><td style=\"background-color:#0f0f1a;border:1px solid #1f1f30;border-radius:10px;padding:18px 20px;\">\n" +
                "                <p style=\"margin:0;font-size:13px;color:#9ca3af;line-height:1.6;\">&#128274; If you didn't request this code, you can safely ignore this email. Never share this code — TBRICKS will never ask for it.</p>\n" +
                "              </td></tr>\n" +
                "            </table>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr><td style=\"padding:0 48px;\"><div style=\"height:1px;background:linear-gradient(90deg,transparent,#2a2a3d,transparent);\"></div></td></tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding:28px 48px 36px;\">\n" +
                "            <p style=\"margin:0 0 4px;font-size:12px;color:#4b5563;\">This is an automated message from TBRICKS. Please do not reply.</p>\n" +
                "            <p style=\"margin:0;font-size:12px;color:#374151;\">&copy; 2025 TBRICKS. All rights reserved.</p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "    </td></tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
    }

    @Data
    private static class EmailRequest {
        @JsonProperty("from")
        private final String from;
        
        @JsonProperty("to")
        private final String to;
        
        @JsonProperty("subject")
        private final String subject;
        
        @JsonProperty("html")
        private final String html;

        public EmailRequest(String from, String to, String subject, String html) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.html = html;
        }
    }

    @Data
    private static class EmailResponse {
        @JsonProperty("id")
        private String id;
    }

    public void sendVerificationLink(String toEmail, String userId) {
        try {
            String verificationLink = buildVerificationLink(userId);
            String htmlContent = buildVerificationLinkEmailHtml(verificationLink);

            EmailRequest emailRequest = new EmailRequest(
                    fromEmail, toEmail, "Verify your TBRICKS account", htmlContent
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<EmailRequest> request = new HttpEntity<>(emailRequest, headers);
            EmailResponse response = restTemplate.postForObject(RESEND_API_URL, request, EmailResponse.class);

            log.info("Verification link sent to email: {}, ID: {}", toEmail, response.getId());
        } catch (Exception e) {
            log.error("Failed to send verification link to email: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification link. Please try again.");
        }
    }

    private String buildVerificationLink(String userId) {
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((userId + ":" + System.currentTimeMillis()).getBytes());
        return verificationBaseUrl + "/verify?token=" + token + "&userId=" + userId;
    }

    private String buildVerificationLinkEmailHtml(String verificationLink) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\"/>\n" +
                "  <style>\n" +
                "    @import url('https://fonts.googleapis.com/css2?family=Space+Mono:wght@400;700&family=Syne:wght@400;700;800&display=swap');\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body style=\"margin:0;padding:0;background-color:#0a0a0f;font-family:'Syne',sans-serif;\">\n" +
                "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#0a0a0f;padding:40px 20px;\">\n" +
                "    <tr><td align=\"center\">\n" +
                "      <table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"\n" +
                "             style=\"max-width:560px;width:100%;border-radius:16px;overflow:hidden;background-color:#13131f;border:1px solid #2a2a3d;\">\n" +
                "        <tr><td style=\"height:4px;background:linear-gradient(90deg,#4f46e5,#7c3aed,#ec4899);font-size:0;\">&nbsp;</td></tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding:40px 48px 32px;background:linear-gradient(160deg,#1a1a2e 0%,#13131f 100%);\">\n" +
                "            <span style=\"font-family:'Syne',sans-serif;font-size:22px;font-weight:800;letter-spacing:0.08em;color:#ffffff;\">T<span style=\"color:#7c3aed;\">&#9632;</span>BRICKS</span>\n" +
                "            <div style=\"height:1px;background:linear-gradient(90deg,#2a2a3d,transparent);margin-top:24px;\"></div>\n" +
                "            <h1 style=\"margin:28px 0 8px;font-family:'Syne',sans-serif;font-size:28px;font-weight:800;color:#ffffff;letter-spacing:-0.01em;\">Verify your account</h1>\n" +
                "            <p style=\"margin:0;font-size:15px;color:#6b7280;line-height:1.6;\">Click the button below to verify your account. This link is only valid for 24 hours.</p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding:0 48px 36px;\">\n" +
                "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "              <tr>\n" +
                "                <td style=\"background:linear-gradient(135deg,#1e1b4b 0%,#1a1a2e 100%);border:1px solid #3730a3;border-radius:12px;padding:32px;text-align:center;\">\n" +
                "                  <p style=\"margin:0 0 24px;font-size:14px;color:#9ca3af;line-height:1.6;\">One click to get started — no code required.</p>\n" +
                "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "                    <tr>\n" +
                "                      <td style=\"background:linear-gradient(135deg,#4f46e5,#7c3aed);border-radius:10px;\">\n" +
                "                        <a href=\"" + verificationLink + "\"\n" +
                "                           style=\"display:inline-block;padding:14px 40px;font-family:'Syne',sans-serif;font-size:15px;font-weight:700;color:#ffffff;text-decoration:none;letter-spacing:0.04em;\">Verify My Account &#8594;</a>\n" +
                "                      </td>\n" +
                "                    </tr>\n" +
                "                  </table>\n" +
                "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" style=\"margin-top:20px;\">\n" +
                "                    <tr><td style=\"background-color:rgba(239,68,68,0.12);border:1px solid rgba(239,68,68,0.25);border-radius:999px;padding:6px 16px;\">\n" +
                "                      <span style=\"font-family:'Space Mono',monospace;font-size:11px;color:#f87171;letter-spacing:0.05em;\">&#9201; Link expires in 24 hours</span>\n" +
                "                    </td></tr>\n" +
                "                  </table>\n" +
                "                  <p style=\"margin:20px 0 0;font-family:'Space Mono',monospace;font-size:10px;color:#4b5563;word-break:break-all;\">If the button doesn't work, copy this link:<br/>\n" +
                "                    <a href=\"" + verificationLink + "\" style=\"color:#6366f1;text-decoration:none;\">" + verificationLink + "</a>\n" +
                "                  </p>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "            </table>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding:0 48px 36px;\">\n" +
                "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "              <tr><td style=\"background-color:#0f0f1a;border:1px solid #1f1f30;border-radius:10px;padding:18px 20px;\">\n" +
                "                <p style=\"margin:0;font-size:13px;color:#9ca3af;line-height:1.6;\">&#128274; If you didn't create a TBRICKS account, you can safely ignore this email. Never share this link — TBRICKS will never ask for it.</p>\n" +
                "              </td></tr>\n" +
                "            </table>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr><td style=\"padding:0 48px;\"><div style=\"height:1px;background:linear-gradient(90deg,transparent,#2a2a3d,transparent);\"></div></td></tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding:28px 48px 36px;\">\n" +
                "            <p style=\"margin:0 0 4px;font-size:12px;color:#4b5563;\">This is an automated message from TBRICKS. Please do not reply.</p>\n" +
                "            <p style=\"margin:0;font-size:12px;color:#374151;\">&copy; 2025 TBRICKS. All rights reserved.</p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "    </td></tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
    }
}
