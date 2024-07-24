package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@SpringBootTest(classes = {
    NotifyClaimantAmendRestitchBundleHandler.class,
    JacksonAutoConfiguration.class
})
class NotifyClaimantAmendRestitchBundleHandlerTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    NotifyClaimantAmendRestitchBundleHandler handler;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).build()).build();
    }

    @ParameterizedTest
    @CsvSource({
        "NO, NO, template-id",
        "YES, NO, bilingual-template-id",
        "NO, YES, ''",
        "YES, YES, ''"
    })
    void shouldSendEmailBasedOnConditions(YesOrNo bilingual, YesOrNo represented, String expectedTemplateId) {
        if (bilingual == YesOrNo.YES) {
            caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(BILINGUAL_TEMPLATE_ID);
        } else {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);
        }
        caseData = caseData.toBuilder().applicant1Represented(represented).build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        if (!expectedTemplateId.isEmpty()) {
            verify(notificationService).sendMail(
                "claimant@hmcts.net",
                expectedTemplateId,
                getNotificationDataMap(caseData),
                "amend-restitch-bundle-claimant-notification-1594901956117591"
            );
        } else {
            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, "John Doe",
            CLAIMANT_V_DEFENDANT, "John Doe V Jack Jackson"
        );
    }
}
