package uk.gov.hmcts.reform.civil.service.notification;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class BulkPrintServiceTest {
    @Mock
    private SendLetterApi sendLetterApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @InjectMocks
    private BulkPrintService bulkPrintService;

    private final String authentication = "Authentication";
    private final byte[] letterTemplate = new byte[]{1, 2, 3};
    private final Letter letter = new Letter(List.of(new Document(
        Base64.getEncoder().encodeToString(letterTemplate),
        Map.of()
    )), BulkPrintService.XEROX_TYPE_PARAMETER);

    @Test
    void shouldSendLetterToBulkPrintSuccessfully() {
        given(authTokenGenerator.generate()).willReturn(authentication);
        bulkPrintService.printLetter(letterTemplate);
        verify(sendLetterApi).sendLetter(refEq(authentication), refEq(letter));
    }

}
