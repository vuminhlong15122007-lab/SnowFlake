package com.javfxtutorial.hethongdaugia.server.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Mã hóa và xác minh mật khẩu")
class PasswordHasherTest {
    @Test
    @DisplayName("hash mật khẩu không lưu plaintext và vẫn xác minh được")
    void hashDoesNotStorePlainTextAndCanBeVerified() {
        String hashed = PasswordHasher.hash("secret123");

        assertNotEquals("secret123", hashed);
        assertTrue(PasswordHasher.isHashed(hashed));
        assertTrue(PasswordHasher.matches("secret123", hashed));
        assertFalse(PasswordHasher.matches("wrong", hashed));
    }

    @Test
    @DisplayName("mật khẩu plaintext cũ vẫn xác minh được để hỗ trợ migrate")
    void legacyPlainTextPasswordStillMatchesForMigration() {
        assertTrue(PasswordHasher.matches("secret123", "secret123"));
        assertFalse(PasswordHasher.matches("wrong", "secret123"));
    }
}
