package com.oronaminc.join.websocket.config;

import java.security.Principal;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StompPrincipal implements Principal {

    private final String name;

    @Override
    public String getName() {
        return name;
    }
}
