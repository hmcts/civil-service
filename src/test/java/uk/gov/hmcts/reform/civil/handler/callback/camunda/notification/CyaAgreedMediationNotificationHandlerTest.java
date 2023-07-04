package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.citizenui.MediationCSVService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    CyaAgreedMediationNotificationHandler.class,
    MediationCSVService.class
})
public class CyaAgreedMediationNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    SendGridClient sendGridClient;

    @MockBean
    MediationCSVEmailConfiguration configuration;

    @Autowired
    MediationCSVService service;

    @Autowired
    private CyaAgreedMediationNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(configuration.getRecipient()).thenReturn("recipient@gmail.com");
            when(configuration.getSender()).thenReturn("sender@gmail.com");
        }

        @Test
        void shouldSendNotification_whenInvoked() {
            CaseData caseData = CaseData.builder()
                .legacyCaseReference("11111111111111")
                .totalClaimAmount(new BigDecimal(9000))
                .applicant1(Party.builder()
                                .type(Party.Type.COMPANY)
                                .companyName("Applicant company name")
                                .partyName("Applicant party name")
                                .partyPhone("0011001100")
                                .partyEmail("applicant@company.com")
                                .build())
                .respondent1(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("Respondent company name")
                                 .partyName("Respondent party name")
                                 .partyPhone("0022002200")
                                 .partyEmail("respondent@company.com")
                                 .build())
                .build();

            CallbackParams params = CallbackParams
                .builder()
                .caseData(caseData)
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_CYA_ON_AGREED_MEDIATION.toString())
                             .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isEqualTo(AboutToStartOrSubmitCallbackResponse.builder().build());
        }
    }
}
