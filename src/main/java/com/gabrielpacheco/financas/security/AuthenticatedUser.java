package com.gabrielpacheco.financas.security;

/**
 * Usuário extraído do token JWT, injetado nos controllers via @AuthenticationPrincipal.
 */
public record AuthenticatedUser(Long id, String email) {
}
