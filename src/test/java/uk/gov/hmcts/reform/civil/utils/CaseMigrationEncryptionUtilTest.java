package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CaseMigrationEncryptionUtilTest {

    @Test
    void shouldEncryptAndDecryptSuccessfully() throws Exception {
        // Arrange
        String secretKeyString = "testSecretKey123";
        String originalData = "This is a test string.";
        SecretKey secretKey = CaseMigrationEncryptionUtil.getKeyFromString(secretKeyString);

        // Act
        String encryptedData = CaseMigrationEncryptionUtil.encrypt(originalData, secretKey);
        String decryptedData = CaseMigrationEncryptionUtil.decrypt(encryptedData, secretKey);

        // Assert
        assertNotNull(encryptedData);
        assertEquals(originalData, decryptedData);
    }

    @Test
    void shouldGenerateKeyFromString() throws Exception {
        // Arrange
        String secretKeyString = "testSecretKey123";

        // Act
        SecretKey secretKey = CaseMigrationEncryptionUtil.getKeyFromString(secretKeyString);

        // Assert
        assertNotNull(secretKey);
    }

    @Test
    void shouldDetectEncryptedFileFormat() throws Exception {
        // Arrange
        String secretKeyString = "testSecretKey123";
        String originalData = "Some test data";
        SecretKey secretKey = CaseMigrationEncryptionUtil.getKeyFromString(secretKeyString);
        String encryptedData = CaseMigrationEncryptionUtil.encrypt(originalData, secretKey);

        Path encryptedFile = Files.createTempFile("encrypted", ".txt");
        Files.writeString(encryptedFile, encryptedData, StandardCharsets.UTF_8);

        // Act & Assert
        assertTrue(CaseMigrationEncryptionUtil.isFileEncrypted(encryptedFile.toString()));

        // Clean up
        Files.deleteIfExists(encryptedFile);
    }

    @Test
    void shouldReturnFalseForInvalidEncryptedFile() throws Exception {
        // Arrange
        Path invalidFile = Files.createTempFile("invalid", ".txt");
        Files.writeString(invalidFile, "This is not encrypted", StandardCharsets.UTF_8);

        // Act & Assert
        assertFalse(CaseMigrationEncryptionUtil.isFileEncrypted(invalidFile.toString()));

        // Clean up
        Files.deleteIfExists(invalidFile);
    }
}
