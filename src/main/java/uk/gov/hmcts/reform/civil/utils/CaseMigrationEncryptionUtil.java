package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
public class CaseMigrationEncryptionUtil implements CommandLineRunner {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // GCM recommended IV size
    private static final int TAG_LENGTH_BIT = 128;

    // Fail-closed by default. Allow only in controlled environments when you truly need a plaintext file.
    private static final boolean ALLOW_PLAINTEXT_DECRYPT_WRITE =
        Boolean.getBoolean("allow.plaintext.decrypt.write");

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
            throw new IllegalArgumentException(errorMessage);
        }
    }

    static void encryptFile(String inputFilePath, String outputFilePath, SecretKey key) throws Exception {
        byte[] inputBytes = Files.readAllBytes(Paths.get(inputFilePath));
        try {
            String content = new String(inputBytes, StandardCharsets.UTF_8);
            String encryptedContent = encrypt(content, key);
            Files.write(Paths.get(outputFilePath), encryptedContent.getBytes(StandardCharsets.UTF_8));
        } finally {
            // Best-effort wipe of plaintext read buffer
            Arrays.fill(inputBytes, (byte) 0);
        }
    }

    /**
     * Secure decrypt that:
     * - Blocks plaintext-to-disk unless -Dallow.plaintext.decrypt.write=true is set.
     * - Creates owner-only temp file (0600 on POSIX), writes there, then atomic-moves into place.
     * - Wipes in-memory buffers where feasible.
     */
    static void decryptFile(String inputFilePath, String outputFilePath, SecretKey key) throws Exception {
        if (!ALLOW_PLAINTEXT_DECRYPT_WRITE) {
            throw new SecurityException(
                "Plaintext decrypt-to-disk is disabled. Run with -Dallow.plaintext.decrypt.write=true in a secure environment.");
        }

        Path in = Paths.get(inputFilePath);
        Path out = Paths.get(outputFilePath);
        Path parent = out.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        byte[] encryptedBytes = Files.readAllBytes(in);
        char[] decryptedChars = null;

        Path tmp = null;
        FileAttribute<Set<PosixFilePermission>> posix0600 = null;
        try {
            // Prepare 0600 permissions when available (POSIX only)
            try {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
                posix0600 = PosixFilePermissions.asFileAttribute(perms);
            } catch (UnsupportedOperationException ignore) {
                // Non-POSIX (e.g., Windows)
            }

            tmp = (posix0600 != null)
                ? Files.createTempFile(parent != null ? parent : Paths.get("."), "dec-", ".tmp", posix0600)
                : Files.createTempFile(parent != null ? parent : Paths.get("."), "dec-", ".tmp");

            // Decrypt (library returns String)
            String encryptedContent = new String(encryptedBytes, StandardCharsets.UTF_8);
            String decryptedContent = decrypt(encryptedContent, key);
            decryptedChars = decryptedContent.toCharArray(); // unavoidable one String; shorten lifetime

            // Write characters directly to temp file
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, WRITE)) {
                writer.write(decryptedChars, 0, decryptedChars.length);
                writer.flush();
            }

            // Atomic move into place
            try {
                Files.move(tmp, out, ATOMIC_MOVE, REPLACE_EXISTING);
                tmp = null; // moved
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, out, REPLACE_EXISTING);
                tmp = null;
            }

            // Best-effort: enforce 0600 on final file (POSIX only)
            if (posix0600 != null) {
                try {
                    Files.setPosixFilePermissions(out, PosixFilePermissions.fromString("rw-------"));
                } catch (UnsupportedOperationException ignore) {
                    // ignore
                }
            }
        } catch (IOException | RuntimeException ex) {
            safeDelete(tmp);
            throw ex;
        } finally {
            if (decryptedChars != null) {
                Arrays.fill(decryptedChars, '\0');
            }
            Arrays.fill(encryptedBytes, (byte) 0);
        }
    }

    public static SecretKey getKeyFromString(String key) throws Exception {
        return generateKey(key);
    }

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = generateIv();
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        String[] parts = encryptedData.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted payload format");
        }
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
        byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        try {
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } finally {
            Arrays.fill(decryptedBytes, (byte) 0);
        }
    }

    public static SecretKeySpec generateKey(String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16); // AES-128
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static boolean isFileEncrypted(String filePath) {
        try {
            String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
            String[] parts = content.split(":");
            if (parts.length != 2) {
                return false;
            }
            Base64.getDecoder().decode(parts[0]);
            Base64.getDecoder().decode(parts[1]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void safeDelete(Path p) {
        if (p == null) {
            return;
        }
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignore) { /* best effort */ }
    }
}
