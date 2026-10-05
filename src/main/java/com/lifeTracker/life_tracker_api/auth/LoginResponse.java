package com.lifeTracker.life_tracker_api.auth;

// Only for non-web clients; the web app never sees its token, which stays in an httpOnly cookie.
public record LoginResponse(String token) {}
