package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ValidateRespondentPaymentDateTest {

    @InjectMocks
    private ValidateRespondentPaymentDate validateRespondentPaymentDate;

    @Mock
    private PaymentDateValidator paymentDateValidator;

    @Mock
    private CallbackParams callbackParams;

    private RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;

    @BeforeEach
    void setUp() {
        respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldReturnErrorsWhenPaymentDateIsInvalid() {
        List<String> errors = Collections.singletonList("Invalid payment date");
        when(paymentDateValidator.validate(respondToClaimAdmitPartLRspec)).thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentPaymentDate.execute(callbackParams);

        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void shouldReturnNoErrorsWhenPaymentDateIsValid() {
        when(paymentDateValidator.validate(respondToClaimAdmitPartLRspec)).thenReturn(Collections.emptyList());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentPaymentDate.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldValidateRespondent2PaymentDateWhenCurrentDefendantIsRespondent2() {
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec2 = new RespondToClaimAdmitPartLRspec();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        caseData.setRespondToClaimAdmitPartLRspec2(respondToClaimAdmitPartLRspec2);
        when(callbackParams.getCaseData()).thenReturn(caseData);

        List<String> errors = Collections.singletonList("Invalid R2 payment date");
        when(paymentDateValidator.validate(respondToClaimAdmitPartLRspec2)).thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateRespondentPaymentDate.execute(callbackParams);

        assertThat(response.getErrors()).isEqualTo(errors);
    }
}
