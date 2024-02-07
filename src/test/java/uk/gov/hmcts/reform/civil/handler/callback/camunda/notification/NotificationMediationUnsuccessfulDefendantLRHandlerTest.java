package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.PARTY_WITHDRAWS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@SpringBootTest(classes = {
    NotificationMediationUnsuccessfulDefendantLRHandler.class,
    OrganisationDetailsService.class,
    JacksonAutoConfiguration.class,
})
class NotificationMediationUnsuccessfulDefendantLRHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @MockBean
    OrganisationDetailsService organisationDetailsService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<String> targetEmail;
    @Captor
    private ArgumentCaptor<String> emailTemplate;
    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;
    @Captor
    private ArgumentCaptor<String> reference;

    @Autowired
    private NotificationMediationUnsuccessfulDefendantLRHandler notificationHandler;

    private static final String ORGANISATION_NAME = "Org Name";
    private static final String EMAIL_TEMPLATE = "test-notification-id";
    private static final String APPLICANT_PARTY_NAME = "Lets party";
    private static final String REFERENCE_NUMBER = "8372942374";
    private static final String CLAIMANT_EMAIL_ADDRESS = "applicantemail@hmcts.net";
    private static final String DEFENDANT_1_EMAIL_ADDRESS = "defendant1email@hmcts.net";
    private static final String DEFENDANT_2_EMAIL_ADDRESS = "defendant2email@hmcts.net";
    private static final Map<String, String> CARM_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME,
                                                                        PARTY_NAME, APPLICANT_PARTY_NAME,
                                                                        CLAIM_REFERENCE_NUMBER, REFERENCE_NUMBER);

    @BeforeEach
    void setUp() {
        given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(EMAIL_TEMPLATE);
        given(organisationDetailsService.getRespondent1LegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
        given(organisationDetailsService.getRespondent2LegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
    }

    @Test
    void shouldSendNotificationToDefendant1LR() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(PARTY_WITHDRAWS)).build())
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        //When
        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_1_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_PROPERTY_MAP);

    }

    @Test
    void shouldSendNotificationToDefendant2LR() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor2EmailAddress(DEFENDANT_2_EMAIL_ADDRESS)
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(PARTY_WITHDRAWS)).build())
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR.name()).build()).build();

        //When
        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_2_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_PROPERTY_MAP);

    }

    @Test
    void shouldNotSendNotification() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor2EmailAddress(DEFENDANT_2_EMAIL_ADDRESS)
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE)).build())
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR.name()).build()).build();

        //When
        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(0)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture());
    }
}
