package com.udemyproject.mobileapp.webservices.service.impl;

import com.udemyproject.mobileapp.webservices.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class emailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final static Logger LOGGER = LoggerFactory.getLogger(emailServiceImpl.class);

    public emailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void send(String to, String email) {
        try{
            MimeMessage mimeMessage =  mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,"utf-8");
            helper.setText(email,true);
            helper.setTo(to);
            helper.setSubject("Confirm email");
            helper.setFrom("unnieputhengadi9@gmail.com");
            mailSender.send(mimeMessage);
        }
        catch (MessagingException e){
            LOGGER.error("failed to send email", e);
            throw new IllegalStateException("failed to send");
        }
    }
}
