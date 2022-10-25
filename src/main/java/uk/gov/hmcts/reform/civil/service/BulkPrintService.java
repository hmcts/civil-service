package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkPrintService {

    public static final String XEROX_TYPE_PARAMETER = "CMC001";
    protected static final String ADDITIONAL_DATA_LETTER_TYPE_KEY = "letterType";
    protected static final String ADDITIONAL_DATA_CASE_IDENTIFIER_KEY = "caseIdentifier";
    protected static final String ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Retryable(
        value = RuntimeException.class,
        backoff = @Backoff(delay = 200)
    )
    public SendLetterResponse printLetter(byte[] letterContent, String claimId, String claimReference, String letterType ) {
        String authorisation = authTokenGenerator.generate();
        LetterWithPdfsRequest letter = generateLetter(additionalInformation(claimId, claimReference, letterType), letterContent);
        log.info("Letter json {}", letter);
        return sendLetterApi.sendLetter(authorisation, letter);
    }

    private LetterWithPdfsRequest generateLetter(Map<String, Object> letterParams, byte[] letterContent) {
        String templateLetter = Base64.getEncoder().encodeToString(letterContent);
        return new LetterWithPdfsRequest(List.of(templateLetter), XEROX_TYPE_PARAMETER, letterParams);
    }

    private Map<String, Object> additionalInformation(String claimId, String claimReference, String letterType) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, letterType);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claimId);
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claimReference);
        return additionalData;
    }
}
