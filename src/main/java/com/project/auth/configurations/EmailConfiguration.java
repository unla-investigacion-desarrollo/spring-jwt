package com.project.auth.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfiguration {
/*
    //Ejemplo de conexion - SES AWS
    @Bean
    public JavaMailSender javaMailSenderAWS() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.starttls.enable", Boolean.TRUE.toString());
        properties.setProperty("mail.smtp.auth", Boolean.TRUE.toString());
        properties.setProperty("mail.smtp.port", String.valueOf(emailPort));
        properties.setProperty("mail.transport.protocol", "smtp");
        javaMailSender.setJavaMailProperties(properties);
        javaMailSender.setProtocol("smtp");
        javaMailSender.setHost(emailHost);
        javaMailSender.setPort(emailPort);
        javaMailSender.setUsername(emailUsername);
        javaMailSender.setPassword(emailPassword);
        return javaMailSender;
    }*/

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setProtocol("smtp");
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        //TODO: Colocar el user y pass de una cuenta de gmail.
        // El mail iria en setUsername y su contraseña en serPassword.
        // Es recomendable guardar estos datos en variables de entrono
        // La cuenta de Gmail debe estar configurada como un servidor de mails para funcionar, de lo contrario,
        // no enviara mails.
        mailSender.setUsername("");
        mailSender.setPassword("");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
