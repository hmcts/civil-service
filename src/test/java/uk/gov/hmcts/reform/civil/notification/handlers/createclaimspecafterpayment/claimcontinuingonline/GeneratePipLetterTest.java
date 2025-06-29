package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.pip.PiPLetterGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class GeneratePipLetterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PiPLetterGenerator pipLetterGenerator;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private Time time;

    @InjectMocks
    private GeneratePipLetter generatePipLetter;

    @Test
    void shouldGenerateAndPrintLetterSuccessfully() {
        when(time.now()).thenReturn(LocalDateTime.now());
        when(pipLetterGenerator.downloadLetter(any(CaseData.class), any(String.class)))
                .thenReturn(new byte[]{1, 2, 3, 4});

        CaseData caseData = CaseData.builder()
                .legacyCaseReference("12345")
                .respondent1(Party.builder().partyName("Test Respondent").type(Party.Type.COMPANY).build())
                .build();
        CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .params(Map.of(CallbackParams.Params.valueOf("BEARER_TOKEN"), "test-token"))
                .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) generatePipLetter
                .handle(params);

        verify(bulkPrintService).printLetter(
                new byte[]{1, 2, 3, 4},
                "12345",
                "12345",
                "first-contact-pack",
                Collections.singletonList(caseData.getRespondent1().getPartyName())
        );
        assertThat(response.getState()).isEqualTo("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");
    }
}