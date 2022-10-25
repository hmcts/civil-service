package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkPrintService {

    public static final String XEROX_TYPE_PARAMETER = "CMC001";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Retryable(
        value = RuntimeException.class,
        backoff = @Backoff(delay = 200)
    )
    public SendLetterResponse printLetter(byte[] letterContent) {
        String authorisation = authTokenGenerator.generate();
        Letter letter = generateLetter(Map.of(), letterContent);
        log.info("Letter json {}", letter);
        return sendLetterApi.sendLetter(authorisation, letter);
    }

    private Letter generateLetter(Map<String, Object> letterParams, byte[] letterContent) {
        String templateLetter = Base64.getEncoder().encodeToString(letterContent);
        Document document = new Document(templateLetter, letterParams);
        return new Letter(List.of(document), XEROX_TYPE_PARAMETER);
    }
}
