package com.tsl.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class SMTPConfig {
//    @Value("${smtp.server.host}")
////    private String smtpHost;
////
////    @Value("${smtp.server.port}")
////    private int smtpPort;
////
////    @Value("${smtp.server.username}")
////    private String username;
////
////    @Value("${smtp.server.password}")
////    private String password;
////
////    @Value("${smtp.server.starttls.enable}")
////    private boolean enableStartTLS;
////
////    public String getSmtpHost() {
////        return smtpHost;
////    }
////
////    public int getSmtpPort() {
////        return smtpPort;
////    }
////
////    public String getUsername() {
////        return username;
////    }
////
////    public String getPassword() {
////        return password;
////    }
////
////    public boolean isEnableStartTLS() {
////        return enableStartTLS;
////    }
////    @Bean
////    public JavaMailSender getJavaMailSender() {
////        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
////        mailSender.setHost(smtpHost);
////        mailSender.setPort(smtpPort);
////        mailSender.setUsername(username);
////        mailSender.setPassword(password);
////
////        Properties properties = mailSender.getJavaMailProperties();
////        properties.put("mail.smtp.auth", "true");
////        properties.put("mail.smtp.starttls.enable", "true");
////
////        return mailSender;
////    }

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
    @PostConstruct
    public void logCredentials() {
        System.out.println("SMTP Username: " + username);
        System.out.println("SMTP Password: " +password);
    }
}
