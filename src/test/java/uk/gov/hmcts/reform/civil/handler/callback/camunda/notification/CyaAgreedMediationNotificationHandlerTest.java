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
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVLrvLipService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCsvServiceFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    CyaAgreedMediationNotificationHandler.class,
    MediationCSVLrvLipService.class
})
public class CyaAgreedMediationNotificationHandlerTest extends BaseCallbackHandlerTest {

    private static final String SENDER = "sender@gmail.com";
    private static final String RECIPIENT = "recipient@gmail.com";

    @MockBean
    private SendGridClient sendGridClient;

    @MockBean
    private MediationCSVEmailConfiguration configuration;

    @MockBean
    private MediationCsvServiceFactory serviceFactory;

    @MockBean
    private MediationCSVLrvLipService mediationCSVLrvLipService;

    @Autowired
    private CyaAgreedMediationNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(mediationCSVLrvLipService.generateCSVContent(any())).thenReturn("some content");
            when(serviceFactory.getMediationCSVService(any())).thenReturn(mediationCSVLrvLipService);
            when(configuration.getRecipient()).thenReturn(SENDER);
            when(configuration.getSender()).thenReturn(RECIPIENT);
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
            assertNotNull(response);
            verify(sendGridClient).sendEmail(anyString(), any());
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder()
                                                 .request(CallbackRequest.builder().eventId(
                                                         "NOTIFY_CYA_ON_AGREED_MEDIATION")
                                                              .build()).build()))
            .isEqualTo("CyaAgreedMediationNotification");
    }
}
