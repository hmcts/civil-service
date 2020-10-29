package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.PaymentsService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {PaymentsCallbackHandler.class})
public class PaymentsCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "RC-1234-1234-1234-1234";

    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    @MockBean
    private PaymentsService paymentsService;

    @Autowired
    private PaymentsCallbackHandler handler;

    @Test
    void shouldMakePbaPayment_whenInvoked() {
        when(paymentsConfiguration.isEnabled()).thenReturn(true);
        when(paymentsService.createCreditAccountPayment(any()))
            .thenReturn(PaymentDto.builder().reference(SUCCESSFUL_PAYMENT_REFERENCE).build());
        CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
        CallbackParams params = callbackParamsOf(new HashMap<>(), ABOUT_TO_SUBMIT)
            .toBuilder().caseData(caseData).build();

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(paymentsService).createCreditAccountPayment(caseData);
        assertThat(response.getData()).extracting("paymentReference").isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
    }
}
