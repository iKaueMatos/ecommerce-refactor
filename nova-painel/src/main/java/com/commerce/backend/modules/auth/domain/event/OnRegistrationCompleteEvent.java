package com.commerce.backend.auth.domain.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import com.commerce.backend.user.infra.entity.User;

@Data
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private User user;
    private String token;

    public OnRegistrationCompleteEvent(User user, String token) {
        super(user);
        this.user = user;
        this.token = token;
    }
}