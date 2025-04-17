package uk.gov.hmcts.reform.civil.service.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
public class IdempotencyKeyGenerator {

    public String generateIdempotencyKey(String messageId, String eventId) {
        log.info("Generating idempotency key for message: {} and event: {}", messageId, eventId);
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(eventId, "eventId cannot be null");
        String concatenatedString = messageId + eventId;
        String idempotencyKey = DigestUtils.md5Hex(concatenatedString).toUpperCase(Locale.ENGLISH);
        log.info("Idempotency key generated: {}", idempotencyKey);
        return idempotencyKey;
    }

}
