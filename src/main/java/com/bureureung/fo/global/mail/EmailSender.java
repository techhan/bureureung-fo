package com.bureureung.fo.global.mail;

public interface EmailSender {

    /**
     * 이메일을 발송한다.
     */
    void send(String to, MailContent content);
}
