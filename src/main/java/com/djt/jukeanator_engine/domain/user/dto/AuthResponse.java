package com.djt.jukeanator_engine.domain.user.dto;

public record AuthResponse(String token, String emailAddress, String role) {
}
