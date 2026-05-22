package com.javfxtutorial.hethongdaugia.server.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    @Test
    @DisplayName("hash null va hash lai chuoi da hash khong lam thay doi du lieu")
    void hashHandlesNullAndAlreadyHashedInput() {
        String hashed = PasswordHasher.hash("secret123");

        assertNull(PasswordHasher.hash(null));
        assertSame(hashed, PasswordHasher.hash(hashed));
    }

    @Test
    @DisplayName("khong match null hoac hash sai format")
    void matchesRejectsNullAndMalformedHash() {
        assertFalse(PasswordHasher.matches(null, "secret123"));
        assertFalse(PasswordHasher.matches("secret123", null));
        assertFalse(PasswordHasher.matches("secret123", "pbkdf2_sha256$bad$format"));
    }

    @Test
    @DisplayName("cung password nhung hash khac nhau do salt khac nhau")
    void hashUsesDifferentSaltForSamePassword() {
        String first = PasswordHasher.hash("secret123");
        String second = PasswordHasher.hash("secret123");

        assertNotEquals(first, second);
        assertTrue(PasswordHasher.matches("secret123", first));
        assertTrue(PasswordHasher.matches("secret123", second));
    }
}
