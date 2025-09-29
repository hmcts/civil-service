package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.EXTERNAL_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

public class DefendantChangeOfAddressClaimantEmailDTOGeneratorTest {

    @InjectMocks
    private DefendantChangeOfAddressClaimantEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private PinInPostConfiguration pipInPostConfiguration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplate() {
        CaseData caseData = CaseData.builder().build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyLiPClaimantDefendantChangedContactDetails()).thenReturn(expectedTemplateId);

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
        String legacyCaseReference = "legacy case reference";
        Long ccdCaseReference = 0L;
        Party party = Party.builder().build();
        CaseData caseData = CaseData.builder()
            .legacyCaseReference(legacyCaseReference)
            .ccdCaseReference(ccdCaseReference)
            .applicant1(party)
            .respondent1(party)
            .build();

        String partyName = "party name";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party)).thenReturn(partyName);

        String url = "url";
        when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn(url);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> actualResults = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(actualResults.size()).isEqualTo(5);
        assertThat(actualResults).containsEntry(CLAIM_REFERENCE_NUMBER, legacyCaseReference);
        assertThat(actualResults).containsEntry(CLAIMANT_NAME, partyName);
        assertThat(actualResults).containsEntry(RESPONDENT_NAME, partyName);
        assertThat(actualResults).containsEntry(FRONTEND_URL, url);
        assertThat(actualResults).containsEntry(EXTERNAL_ID, ccdCaseReference.toString());
    }

    @Test
    void shouldNotifyWhenApplicantIsNotRepresentedAndNotSpecAoSApplicantCorrespondenceAddressRequire() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).specAoSApplicantCorrespondenceAddressRequired(NO).build();
        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyWhenApplicantIsRepresented() {
        CaseData caseData = CaseData.builder().applicant1Represented(YES).specAoSApplicantCorrespondenceAddressRequired(NO).build();
        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenSpecAoSApplicantCorrespondenceAddressRequire() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).specAoSApplicantCorrespondenceAddressRequired(YES).build();
        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenApplicantIsRepresentedAndSpecAoSApplicantCorrespondenceAddressRequire() {
        CaseData caseData = CaseData.builder().applicant1Represented(YES).specAoSApplicantCorrespondenceAddressRequired(YES).build();
        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }
}
