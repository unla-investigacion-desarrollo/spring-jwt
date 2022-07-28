package com.project.auth.services.impl;

import com.project.auth.constants.SecurityConfigConstants;
import com.project.auth.dtos.mail.DataMailAddUserDTO;
import com.project.auth.dtos.mail.DataMailRecoveryDTO;
import com.project.auth.models.database.Users;
import com.project.auth.repositories.UserRepository;
import com.project.auth.services.IMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.ISpringTemplateEngine;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class MailService implements IMailService {

    private static final String IMAGE_PATH = "static/img/";

    private static final String IMAGE_BRAND_HORIZONTAL = "LogoExample.png";

    private static final String INITIAL_EMAIL_LOG = "Sending email to {} ...";

    private static final String FINAL_EMAIL_LOG = "Error sending email";

    private final UserRepository userRepository;

    private final JavaMailSender javaMailSender;

    private final ISpringTemplateEngine templateEngine;

    @Value("${mail.support}")
    private String supportMail;

    @Value("${web.url}")
    private String webUrl;

    public MailService(UserRepository userRepository,
                       JavaMailSender javaMailSender, ISpringTemplateEngine templateEngine) {
        this.userRepository = userRepository;
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * This method sends an email to desired recipients to handle the recover password. Then, after
     * sending the mail, configure the user with the first sign-in mark in true.
     *
     * @param emailsTo          (email recipients)
     * @param username          (username)
     * @param temporaryPassword (Password that must be changed on the first login)
     * @param user              {@link Users}
     */
    @Override
    public void sendRecoveryPassEmail(List<String> emailsTo, String username,
                                      String temporaryPassword, Users user) {
        log.info(INITIAL_EMAIL_LOG, emailsTo);
        try {

            String htmlText = loadAndFillTemplate(MailType.RECOVERY,
                    getDataForRecovery(username, temporaryPassword));
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(IMAGE_BRAND_HORIZONTAL, "image");
            javaMailSender.send(buildMail(SecurityConfigConstants.RECOVERY_PASS_MAIL_SUBJECT,
                    htmlText, emailsTo, hashMap));
            log.info("User: {} - email sent", username);

            user.setFailedAttempts(0);
            userRepository.save(user);

        } catch (MailException | MessagingException | IOException e) {
            log.error(FINAL_EMAIL_LOG, e);
            throw new MailSendException(e.getCause().getMessage());
        }
    }

    /**
     * This method sends an email to the intended recipients to welcome a new user. Then, after
     * sending the mail, configure the user with the first sign-in mark in true.
     *
     * @param emailsTo          (email recipients)
     * @param username          (username)
     * @param temporaryPassword (Password that must be changed on the first login)
     * @param user              {@link Users}
     */
    @Override
    public void sendNewUserEmail(List<String> emailsTo, String username,
                                 String temporaryPassword, Users user) {
        log.info(INITIAL_EMAIL_LOG, emailsTo);
        try {

            String htmlText = loadAndFillTemplate(MailType.ADD_USER,
                    getDataMailAddUser(user.getFirstName(), username, temporaryPassword));
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(IMAGE_BRAND_HORIZONTAL, "image");
            javaMailSender.send(buildMail(SecurityConfigConstants.NEW_USER_MAIL_SUBJECT, htmlText,
                    emailsTo, hashMap));
            log.info("User: {} - email sent", username);
            userRepository.save(user);

        } catch (MailException | MessagingException | IOException e) {
            log.error(FINAL_EMAIL_LOG, e);
            throw new MailSendException(e.getCause().getMessage());
        }
    }

    /**
     * Method in charge of building an email. First create a MimeMessage and set all the basic data
     * for mail (from, to, subject). Later, create a MimeMultipart to set all the content data (text
     * and images) and then it is set into the MimeMessage content.
     *
     * @param subject  (email subject)
     * @param htmlText (email content)
     * @param emailsTo (email recipients)
     * @return {@link MimeMessage}
     */
    private MimeMessage buildMail(String subject, String htmlText, List<String> emailsTo,
                                  HashMap<String, String> images)
            throws MessagingException, IOException {

        MimeMessage message = javaMailSender.createMimeMessage();
        String emailsToSeparatedByComma = String.join(",", emailsTo);
        message.addRecipients(RecipientType.TO, emailsToSeparatedByComma);
        message.setFrom(SecurityConfigConstants.EMAIL_NAME + " <"
                + SecurityConfigConstants.EMAIL_FROM + ">");
        message.setSubject(subject);

        MimeMultipart multipart = new MimeMultipart("related");

        // first part (the html)
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlText, "text/html; charset=ISO-8859-1");
        multipart.addBodyPart(messageBodyPart);

        // second part (the images)
        for (Map.Entry<String, String> entry : images.entrySet()) {
            ClassLoader classLoader = this.getClass().getClassLoader();
            InputStream stream = Objects.requireNonNull(
                    classLoader.getResourceAsStream(IMAGE_PATH + entry.getKey()));

            messageBodyPart = new MimeBodyPart();
            DataSource fds = new ByteArrayDataSource(stream, "image/png");
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID", "<" + entry.getValue() + ">");

            // add image to the multipart
            multipart.addBodyPart(messageBodyPart);
        }
        message.setContent(multipart);

        log.info("Mail built successfully.");
        return message;
    }

    /**
     * This method receives a MailType enum to choose the correct html template and then the
     * TemplateEngine processes it with the desired data inside depending the enum value.
     *
     * @param type (email type)
     * @param data (email data content)
     * @return htmlText
     */
    public String loadAndFillTemplate(MailType type, Object data) {

        Context context = new Context();

        switch (type.name()) {
            case "RECOVERY":
                context.setVariable("data", data);
                return templateEngine.process("mail_recovery", context);

            case "ADD_USER":
                context.setVariable("data", data);
                return templateEngine.process("mail_new_user", context);

            default:
                throw new IllegalStateException("Unexpected value: " + type.name());
        }

    }

    private DataMailRecoveryDTO getDataForRecovery(String username, String temporaryPassword) {
        return new DataMailRecoveryDTO(username, temporaryPassword, supportMail, webUrl);
    }

    private DataMailAddUserDTO getDataMailAddUser(String name, String username,
                                                  String temporaryPassword) {
        return new DataMailAddUserDTO(name, username, temporaryPassword, supportMail, webUrl);
    }

    private enum MailType {
        RECOVERY,
        ADD_USER
    }

}
