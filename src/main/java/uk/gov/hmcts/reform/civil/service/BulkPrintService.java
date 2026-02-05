package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkPrintService {

    public static final String XEROX_TYPE_PARAMETER = "CMC001";
    protected static final String ADDITIONAL_DATA_LETTER_TYPE_KEY = "letterType";
    protected static final String ADDITIONAL_DATA_CASE_IDENTIFIER_KEY = "caseIdentifier";
    protected static final String ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    protected static final String RECIPIENTS = "recipients";
    protected static final String FILE_NAMES = "fileNames";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;

    /**
     * Sends a letter with the specified content and additional metadata.
     * The letter is sent to the bulk print service for processing.
     *
     * @param letterContent the content of the letter as a byte array
     * @param claimId the identifier of the claim associated with the letter
     * @param claimReference the reference number of the claim
     * @param letterType the type/category of the letter
     * @param personList a list of recipient person names
     * @param filenames a list of file names associated with the letter, optional
     * @return a response from the bulk print service containing details of the letter sent
     */
    @Retryable(
        value = RuntimeException.class,
        backoff = @Backoff(delay = 200)
    )
    public SendLetterResponse printLetter(byte[] letterContent, String claimId,
                                          String claimReference, String letterType, List<String> personList, List<String> filenames) {
        log.info("Printing letter for claimId: {}", claimReference);
        String authorisation = authTokenGenerator.generate();
        LetterWithPdfsRequest letter =
            generateLetter(additionalInformation(claimId, claimReference, letterType, personList, filenames), letterContent);
        return sendLetterApi.sendLetter(authorisation, letter);
    }

    private LetterWithPdfsRequest generateLetter(Map<String, Object> letterParams, byte[] letterContent) {
        String templateLetter = Base64.getEncoder().encodeToString(letterContent);
        return new LetterWithPdfsRequest(List.of(templateLetter), XEROX_TYPE_PARAMETER, letterParams);
    }

    private Map<String, Object> additionalInformation(String claimId, String claimReference, String letterType,
                                                      List<String> personList,
                                                      List<String> filenames) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, letterType);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claimId);
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claimReference);
        additionalData.put(RECIPIENTS, personList);

        if (isNotEmpty(filenames)) {
            additionalData.put(FILE_NAMES, filenames);
        }
        return additionalData;
    }
}
