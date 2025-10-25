package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.common.errors.UserNotFoundException;
import com.alhakim.ecommerce.config.SendgridConfig;
import com.alhakim.ecommerce.entity.Order;
import com.alhakim.ecommerce.entity.User;
import com.alhakim.ecommerce.repository.UserRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SendgridConfig sendgridConfig;
    private final SendGrid sendGrid;
    private final UserRepository userRepository;
    private final Retry emailRetry;

    @Async
    @Override
    public void notifySuccessfulPayment(Order order) {
        User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new UserNotFoundException("User Not Found"));
        Mail mail = prepareSuccessfulPaymentMail(user, order);
        sendEmailRetry(mail);
    }

    @Async
    @Override
    public void notifyFailedPayment(Order order) {
        User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new UserNotFoundException("User Not Found"));
        Mail mail = prepareFailedPaymentMail(user, order);
        sendEmailRetry(mail);
    }

    private Mail prepareSuccessfulPaymentMail(User user, Order order) {
        return getMail(user, order, true);
    }

    private Mail prepareFailedPaymentMail(User user, Order order) {
        return getMail(user, order, false);
    }

    private Mail getMail(User user, Order order, boolean success) {
        Email from = new Email(sendgridConfig.getFromEmail());
        String subject = "Sending with SendGrid is Fun";
        Email to = new Email(user.getEmail());
        String message;
        if (success) {
            message = "Order with id " + order.getOrderId() + " with a total of " + order.getTotalAmount() + " is successful";
        } else {
            message = "Order with id " + order.getOrderId() + " current status is " + order.getStatus();
        }

        Content content = new Content("text/plain", message);
        return new Mail(from, subject, to, content);
    }

    private void sendEmail(Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        System.out.println(response.getHeaders());
        if (response.getStatusCode() > 299) {
            throw new IOException("Failed to send email");
        }
    }

    private void sendEmailRetry(Mail mail) {
        try {
            emailRetry.executeCallable(() -> {
                sendEmail(mail);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
