package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.service.BulkPrintService.ADDITIONAL_DATA_CASE_IDENTIFIER_KEY;
import static uk.gov.hmcts.reform.civil.service.BulkPrintService.ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY;
import static uk.gov.hmcts.reform.civil.service.BulkPrintService.ADDITIONAL_DATA_LETTER_TYPE_KEY;
import static uk.gov.hmcts.reform.civil.service.BulkPrintService.XEROX_TYPE_PARAMETER;

@ExtendWith(SpringExtension.class)
class BulkPrintServiceTest {

    @Mock
    private SendLetterApi sendLetterApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @InjectMocks
    private BulkPrintService bulkPrintService;

    private final String authentication = "Authentication";
    private final String letterType = "Letter type";
    private final String claimId = "1";

    private final Map<String, Object> additionalInformation =
        Map.of(ADDITIONAL_DATA_LETTER_TYPE_KEY, letterType,
               ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claimId,
               ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claimId);
    private final byte[] letterTemplate = new byte[]{1, 2, 3};
    private final LetterWithPdfsRequest letter =
        new LetterWithPdfsRequest(List.of(Base64.getEncoder().encodeToString(letterTemplate)), XEROX_TYPE_PARAMETER, additionalInformation);

    @Test
    void shouldSendLetterToBulkPrintSuccessfully() {
        given(authTokenGenerator.generate()).willReturn(authentication);
        bulkPrintService.printLetter(letterTemplate, claimId, claimId, letterType);
        verify(sendLetterApi).sendLetter(refEq(authentication), refEq(letter));
    }

}
