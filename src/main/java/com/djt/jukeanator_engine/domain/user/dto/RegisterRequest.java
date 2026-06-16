package com.djt.jukeanator_engine.domain.user.dto;

public record RegisterRequest(String firstName, String lastName, String emailAddress,
    String password) {
}
