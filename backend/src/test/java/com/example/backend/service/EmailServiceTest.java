package com.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = mock(MimeMessage.class);
    }

    @Test
    void sendSimpleMail_success() throws Exception {
        String to = "test@sis.hust.edu.vn";
        String subject = "Test Subject";
        String body = "Test Body";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(ArgumentMatchers.<MimeMessage>any());

        assertDoesNotThrow(() -> {
            emailService.sendSimpleMail(to, subject, body);
        });

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(ArgumentMatchers.<MimeMessage>any());
    }

    @Test
    void sendSimpleMail_mailException() {
        String to = "test@sis.hust.edu.vn";
        String subject = "Test Subject";
        String body = "Test Body";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailException("Mail server error") {
        }).when(mailSender).send(ArgumentMatchers.<MimeMessage>any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendSimpleMail(to, subject, body);
        });

        assertTrue(exception.getMessage().startsWith("Failed to send email"));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(ArgumentMatchers.<MimeMessage>any());
    }

    @Test
    void sendSimpleMail_messagingException() throws Exception {
        String to = "test@sis.hust.edu.vn";
        String subject = "Test Subject";
        String body = "Test Body";

        // MessagingException can be thrown during MimeMessageHelper construction
        // We'll test it by making send() throw a RuntimeException that wraps
        // MessagingException
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        RuntimeException wrappedException = new RuntimeException("Failed to send email",
                new MessagingException("Messaging error"));
        doThrow(wrappedException).when(mailSender).send(ArgumentMatchers.<MimeMessage>any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendSimpleMail(to, subject, body);
        });

        assertTrue(exception.getMessage().startsWith("Failed to send email"));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(ArgumentMatchers.<MimeMessage>any());
    }
}
