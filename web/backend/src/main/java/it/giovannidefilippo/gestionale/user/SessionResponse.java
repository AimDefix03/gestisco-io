package it.giovannidefilippo.gestionale.user;

import java.time.Instant;

/** Sessione restituita al browser: il token viaggia solo nel cookie `HttpOnly` e non compare nel corpo. */
public record SessionResponse(UserResponse user, Instant expiresAt) {
    static SessionResponse from(AuthSessionResponse session) {
        return new SessionResponse(session.user(), session.expiresAt());
    }
}
