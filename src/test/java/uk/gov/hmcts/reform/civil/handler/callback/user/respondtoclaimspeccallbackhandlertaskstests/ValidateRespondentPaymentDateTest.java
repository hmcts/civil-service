package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidateRespondentPaymentDateTest {

    @InjectMocks
    private ValidateRespondentPaymentDate validateRespondentPaymentDate;

    @Mock
    private PaymentDateValidator paymentDateValidator;

    @Mock
    private CallbackParams callbackParams;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldReturnErrorsWhenPaymentDateIsInvalid() {
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = RespondToClaimAdmitPartLRspec.builder().build();
        caseData = caseData.toBuilder().respondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec).build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        List<String> errors = Collections.singletonList("Invalid payment date");
        when(paymentDateValidator.validate(respondToClaimAdmitPartLRspec)).thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentPaymentDate.execute(callbackParams);

        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void shouldReturnNoErrorsWhenPaymentDateIsValid() {
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = RespondToClaimAdmitPartLRspec.builder().build();
        caseData = caseData.toBuilder().respondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec).build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        when(paymentDateValidator.validate(respondToClaimAdmitPartLRspec)).thenReturn(Collections.emptyList());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentPaymentDate.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }
}
