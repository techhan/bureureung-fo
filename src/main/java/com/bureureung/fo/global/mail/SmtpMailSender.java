package com.bureureung.fo.global.mail;

import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpMailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(String to, MailContent content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(content.subject());
            helper.setText(content.body(), true); // true : HTML 허용

            javaMailSender.send(message);
            log.info("메일 발송 성공: to={}, subject={}", to, content.subject());
        } catch (Exception e) {
            log.error("메일 발송 실패: to={}", to, e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
