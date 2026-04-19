package com.bureureung.fo.global.mail;

public record MailContent(String subject, String body) {

    public static MailContent verificationCode(String code) {
        String subject = "[부르릉] 이메일 인증 코드 전송";
        String body = """
                <h1>이메일 인증 코드</h1>
                <p>인증 코드: <strong>%s</strong></p>
                <p>5분 안에 입력해주세요.</p>
                """.formatted(code);
        return new MailContent(subject, body);
    }
}
