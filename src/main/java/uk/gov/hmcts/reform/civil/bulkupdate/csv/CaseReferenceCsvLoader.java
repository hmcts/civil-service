package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@NoArgsConstructor
@Component
public class CaseReferenceCsvLoader {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // GCM recommended IV size
    private static final int TAG_LENGTH_BIT = 128;

    @SuppressWarnings({"java:S3740", "java:S1488"})
    public <T extends CaseReference> List<T> loadCaseReferenceList(Class<T> type, String fileName) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.typedSchemaFor(type).withHeader();
            MappingIterator<T> it = new CsvMapper().readerFor(type)
                .with(csvSchema)
                .readValues(getClass().getClassLoader().getResource(fileName));
            List<T> list = it.readAll();

            return list;
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            return Collections.emptyList();
        }
    }

    public <T extends CaseReference> List<T> loadCaseReferenceList(Class<T> type, String fileName, String secret) {
        try {
            String encryptedContent = new String(
                Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI()))
            );
            SecretKey key = getKeyFromString(secret);
            String decryptedContent = decrypt(encryptedContent, key);

            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.typedSchemaFor(type).withHeader();

            MappingIterator<T> it = csvMapper.readerFor(type)
                .with(csvSchema)
                .readValues(decryptedContent);

            return it.readAll();
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            return Collections.emptyList();
        }
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

    private static SecretKeySpec generateKey(String key) throws Exception {
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
}
