package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.aos.AcknowledgementOfClaimGeneratorForSpec;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    GenerateAcknowledgementOfClaimForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class GenerateAcknowledgementOfClaimForSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private GenerateAcknowledgementOfClaimForSpecCallbackHandler handler;

    @MockBean
    private AcknowledgementOfClaimGeneratorForSpec acknowledgementOfClaimGenerator;

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();

        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // Then
        assertThat(handler.camundaActivityId(params)).isEqualTo("AcknowledgeClaimGenerateAcknowledgementOfClaimForSpec");
    }
}
