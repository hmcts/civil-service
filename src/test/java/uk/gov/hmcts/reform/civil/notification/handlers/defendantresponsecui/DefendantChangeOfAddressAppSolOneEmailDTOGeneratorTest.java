package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

public class DefendantChangeOfAddressAppSolOneEmailDTOGeneratorTest {

    @InjectMocks
    private DefendantChangeOfAddressAppSolOneEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplate() {
        CaseData caseData = CaseData.builder().build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentChangeOfAddressNotificationTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("defendant-contact-details-change-applicant-notification-%s");
    }

    @Test
    void shouldReturnCorrectCustomProperties() {
        Party party = Party.builder().build();
        CaseData caseData = CaseData.builder().respondent1(party).build();

        String legalOrg = "legal org";
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(legalOrg);

        String partyName = "party name";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party)).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> actualResults = emailDTOGenerator.addCustomProperties(properties, caseData);

        notificationUtilsMockedStatic.close();
        partyUtilsMockedStatic.close();

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrg);
        assertThat(actualResults).containsEntry(RESPONDENT_NAME, partyName);
    }

    @Test
    void shouldNotifyWhenApplicantIsRepresentedAndNotSpecAoSApplicantCorrespondenceAddressRequire() {
        CaseData caseData = CaseData.builder().applicant1Represented(YES).specAoSApplicantCorrespondenceAddressRequired(NO).build();
        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyWhenApplicantIsNotRepresented() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).specAoSApplicantCorrespondenceAddressRequired(NO).build();
        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenSpecAoSApplicantCorrespondenceAddressRequire() {
        CaseData caseData = CaseData.builder().applicant1Represented(YES).specAoSApplicantCorrespondenceAddressRequired(YES).build();
        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenApplicantIsNotRepresentedAndSpecAoSApplicantCorrespondenceAddressRequire() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).specAoSApplicantCorrespondenceAddressRequired(YES).build();
        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }
}
