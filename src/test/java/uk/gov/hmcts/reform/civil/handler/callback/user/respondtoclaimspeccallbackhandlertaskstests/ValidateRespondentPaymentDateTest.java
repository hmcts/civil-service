package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler.ValidateRespondentPaymentDate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
        respondToClaimAdmitPartLRspec = RespondToClaimAdmitPartLRspec.builder().build();
        CaseData caseData = CaseData.builder().respondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec).build();
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
}