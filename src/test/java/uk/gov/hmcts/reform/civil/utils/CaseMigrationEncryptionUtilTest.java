package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void shouldDetectEncryptedFileFormat() {
        // Arrange
        String validEncryptedContent = "dGVzdElWOnRlc3RFbmNyeXB0ZWREYXRh"; // Base64 encoded IV:data
        String invalidContent = "InvalidContent";

        // Act & Assert
        assertEquals(true, CaseMigrationEncryptionUtil.isFileEncrypted(validEncryptedContent));
        assertEquals(false, CaseMigrationEncryptionUtil.isFileEncrypted(invalidContent));
    }
}
