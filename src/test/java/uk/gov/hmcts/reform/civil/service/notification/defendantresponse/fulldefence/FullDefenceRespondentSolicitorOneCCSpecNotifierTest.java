package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
 class FullDefenceRespondentSolicitorOneCCSpecNotifierTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private FullDefenceRespondentSolicitorOneCCSpecNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
    }

    @Test
    void shouldNotifyRespondentSolicitorSpecDef1_whenInvokedWithCcEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("spec-respondent-template-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "spec-respondent-template-id",
            getNotificationDataMapPartAdmissionSpec(),
            "defendant-response-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitorSpecDef1SecondScenerio_whenInvokedWithCcEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();
        CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                    .build())
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("spec-respondent-template-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("respondentsolicitor2@example.com"),
            ArgumentMatchers.eq("spec-respondent-template-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    private Map<String, String> getNotificationDataMapPartAdmissionSpec() {
        return Map.of(
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE
        );
    }

    private Map<String, String> getNotificationDataMapSpec() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name"
        );
    }
}
