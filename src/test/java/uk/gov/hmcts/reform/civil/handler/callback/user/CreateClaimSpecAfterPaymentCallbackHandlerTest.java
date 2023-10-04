package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;

@SpringBootTest(classes = {
    CreateClaimSpecAfterPaymentCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class CreateClaimSpecAfterPaymentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private CreateClaimSpecAfterPaymentCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    void shouldRespondWithStateChanged() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CLAIM_SPEC_AFTER_PAYMENT);
    }

    @Test
    void shouldUpdateRespondent1ResponseDeadlineTo28days_whenClaimIssued() {

        LocalDateTime localDateTime = LocalDateTime.of(2023, 10, 4, 12, 0, 0);
        when(deadlinesCalculator.plus28DaysAt4pmDeadline(any())).thenReturn(localDateTime);
        when(toggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();
        caseData = caseData.toBuilder().ccdState(CASE_ISSUED).build();
        CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(localDateTime);

    }

    @Test
    void shouldNotUpdateRespondent1ResponseDeadlineTo28days_whenClaimIssued() {

        when(toggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();
        caseData = caseData.toBuilder().ccdState(CASE_ISSUED).build();
        CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertNull(updatedData.getRespondent1ResponseDeadline());

    }

    @Test
    void shouldNotUpdateRespondent1ResponseDeadlineTo28days_whenLRClaimIssued() {

        when(toggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .build();
        caseData = caseData.toBuilder().ccdState(CASE_ISSUED).build();
        CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertNull(updatedData.getRespondent1ResponseDeadline());

    }
}
