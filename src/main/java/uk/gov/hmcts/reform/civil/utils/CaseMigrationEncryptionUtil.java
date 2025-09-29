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

    private static final String ALGO = "AES";
    private static final String XFORM = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_BITS = 128;

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            log.info("Usage: java CaseMigrationEncryptionUtil <encrypt|rekey> <inputFilePath> <outputFilePath> <secretKey>");
            log.info("For rekey with a NEW key, pass it as the 5th arg: <newSecretKey>");
            return;
        }
        String op = args[0];
        String in = args[1];
        String out = args[2];
        String oldKeyStr = args[3];
        String newKeyStr = args.length >= 5 ? args[4] : oldKeyStr; // default: same key (normalize)

        processFile(oldKeyStr, newKeyStr, op, in, out);
    }

    @Override
    public void run(String... args) {
        SpringApplication.run(CaseMigrationEncryptionUtil.class, args);
    }

    static void processFile(String oldKeyStr, String newKeyStr, String op, String input, String output) throws Exception {
        SecretKey oldKey = getKeyFromString(oldKeyStr);
        SecretKey newKey = getKeyFromString(newKeyStr);

        switch (op.toLowerCase()) {
            case "encrypt" -> {
                byte[] plain = Files.readAllBytes(Paths.get(input));
                try {
                    String enc = encrypt(new String(plain, StandardCharsets.UTF_8), newKey);
                    Files.write(Paths.get(output), enc.getBytes(StandardCharsets.UTF_8)); // ciphertext to disk: OK
                } finally {
                    Arrays.fill(plain, (byte) 0);
                }
                log.info("File encrypted successfully. {} -> {}", input, output);
            }
            case "rekey" -> {
                // Read ciphertext, decrypt in memory, re-encrypt immediately, write ciphertext
                String encIn = Files.readString(Paths.get(input), StandardCharsets.UTF_8);
                String dec = decrypt(encIn, oldKey);
                try {
                    String encOut = encrypt(dec, newKey);
                    Files.write(Paths.get(output), encOut.getBytes(StandardCharsets.UTF_8)); // ciphertext to disk: OK
                } finally {
                    // Best-effort wipe of plaintext in memory
                    char[] decChars = dec.toCharArray();
                    Arrays.fill(decChars, '\0');
                }
                log.info("File re-encrypted (rekeyed) successfully. {} -> {}", input, output);
            }
            default -> throw new IllegalArgumentException("Invalid operation. Use 'encrypt' or 'rekey'.");
        }
    }

    public static SecretKey getKeyFromString(String key) throws Exception {
        return generateKey(key);
    }

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(XFORM);
        byte[] iv = generateIv();
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] enc = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(enc);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        String[] parts = encryptedData.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted payload format");
        }
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] ct = Base64.getDecoder().decode(parts[1]);
        Cipher cipher = Cipher.getInstance(XFORM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] pt = cipher.doFinal(ct);
        try {
            return new String(pt, StandardCharsets.UTF_8);
        } finally {
            Arrays.fill(pt, (byte) 0);
        }
    }

    public static SecretKeySpec generateKey(String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] kb = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        kb = Arrays.copyOf(kb, 16); // AES-128
        return new SecretKeySpec(kb, ALGO);
    }

    public static boolean isFileEncrypted(String filePath) {
        try {
            // Read text as UTF-8 (your encrypt/decrypt use UTF-8)
            String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);

            // Be tolerant of accidental whitespace/newlines around the payload
            content = content.trim();
            int sep = content.indexOf(':');
            if (sep <= 0 || sep == content.length() - 1) {
                return false; // must have IV + ":" + ciphertext
            }

            // Validate IV part: must be valid Base64 and decode to exactly 12 bytes (GCM IV size you use)
            String ivB64 = content.substring(0, sep);
            byte[] iv = Base64.getDecoder().decode(ivB64);
            if (iv.length != IV_SIZE) {
                return false;
            }

            // Validate ciphertext part: must be valid Base64 and non-empty
            String ctB64 = content.substring(sep + 1).trim();
            if (ctB64.isEmpty()) {
                return false;
            }
            Base64.getDecoder().decode(ctB64);

            return true;
        } catch (Exception e) {
            // Any decode/IO error -> treat as not encrypted (or unknown)
            return false;
        }
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
