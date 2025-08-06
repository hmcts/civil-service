package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
public class CaseMigrationEncryptionUtil implements CommandLineRunner {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // GCM recommended IV size
    private static final int TAG_LENGTH_BIT = 128;

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            log.info("Usage: java caseMigrationEncryptionUtil <encrypt|decrypt> <inputFilePath> <outputFilePath> <secretKey>");
            return;
        }

        String operation = args[0];
        String inputFilePath = args[1];
        String outputFilePath = args[2];
        String secretKeyString = args[3];

        processFile(secretKeyString, operation, inputFilePath, outputFilePath);
    }

    @Override
    public void run(String... args) {
        SpringApplication.run(CaseMigrationEncryptionUtil.class, args);
    }

    static void processFile(String secretKeyString, String operation, String inputFilePath, String outputFilePath) throws Exception {
        SecretKey key = getKeyFromString(secretKeyString);

        if ("encrypt".equalsIgnoreCase(operation)) {
            encryptFile(inputFilePath, outputFilePath, key);
            log.info("File encrypted successfully. {} -> {}", inputFilePath, outputFilePath);
        } else if ("decrypt".equalsIgnoreCase(operation)) {
            decryptFile(inputFilePath, outputFilePath, key);
            log.info("File decrypted successfully. {} -> {}", inputFilePath, outputFilePath);
        } else {
            String errorMessage = "Invalid operation. Use 'encrypt' or 'decrypt'.";
            log.error(errorMessage);
            throw new Exception(errorMessage);
        }
    }

    static void encryptFile(String inputFilePath, String outputFilePath, SecretKey key) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(inputFilePath)));
        String encryptedContent = encrypt(content, key);
        Files.write(Paths.get(outputFilePath), encryptedContent.getBytes());
    }

    static void decryptFile(String inputFilePath, String outputFilePath, SecretKey key) throws Exception {
        String encryptedContent = new String(Files.readAllBytes(Paths.get(inputFilePath)));
        String decryptedContent = decrypt(encryptedContent, key);
        Files.write(Paths.get(outputFilePath), decryptedContent.getBytes());
    }

    public static SecretKey getKeyFromString(String key) throws Exception {
        SecretKeySpec secretKey = generateKey(key);
        return secretKey;
    }

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = generateIv();
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        String[] parts = encryptedData.split(":");
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
        byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    public static SecretKeySpec generateKey(String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16); // Use first 16 bytes for AES-128
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static boolean isFileEncrypted(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            String[] parts = content.split(":");

            if (parts.length != 2) {
                return false; // Not in the expected format
            }
            // Try decoding both parts as Base64
            Base64.getDecoder().decode(parts[0]);
            Base64.getDecoder().decode(parts[1]);

            return true;
        } catch (Exception e) {
            return false; // Decoding failed or IO error -> not encrypted
        }
    }
}
