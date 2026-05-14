package com.javfxtutorial.hethongdaugia.server.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {
    @Test
    void hashDoesNotStorePlainTextAndCanBeVerified() {
        String hashed = PasswordHasher.hash("secret123");

        assertNotEquals("secret123", hashed);
        assertTrue(PasswordHasher.isHashed(hashed));
        assertTrue(PasswordHasher.matches("secret123", hashed));
        assertFalse(PasswordHasher.matches("wrong", hashed));
    }

    @Test
    void legacyPlainTextPasswordStillMatchesForMigration() {
        assertTrue(PasswordHasher.matches("secret123", "secret123"));
        assertFalse(PasswordHasher.matches("wrong", "secret123"));
    }
}
