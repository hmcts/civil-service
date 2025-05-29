package uk.gov.hmcts.reform.civil.notification.handlers.defendantsignsettlementagreement;

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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class DefendantSignSettlementDefendantEmailDTOGeneratorTest {

    @InjectMocks
    private DefendantSignSettlementDefendantEmailDTOGenerator emailDTOGenerator;

    @Mock
    private PinInPostConfiguration pipInPostConfiguration;

    @Mock
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenDefendantSignedSettlementAgreement() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder().respondentSignSettlementAgreement(YES).build();
        CaseData caseData = CaseData.builder().caseDataLiP(caseDataLiP).build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyRespondentForSignedSettlementAgreement()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenDefendantSignedSettlementNotAgreed() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder().respondentSignSettlementAgreement(NO).build();
        CaseData caseData = CaseData.builder().caseDataLiP(caseDataLiP).build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyRespondentForNotAgreedSignSettlement()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("notify-signed-settlement-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Party party = Party.builder().build();
        String caseReference = "caseReference";
        CaseData caseData = CaseData.builder().applicant1(party).respondent1(party).legacyCaseReference(caseReference).build();
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);

        String partyName = "party name";
        partyUtilsMockedStatic.when(() -> getPartyNameBasedOnType(party, false)).thenReturn(partyName);
        String url = "url";
        when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn(url);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(4);
        assertThat(updatedProperties).containsEntry(CLAIM_REFERENCE_NUMBER, caseReference);
        assertThat(updatedProperties).containsEntry(CLAIMANT_NAME, partyName);
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, partyName);
        assertThat(updatedProperties).containsEntry(FRONTEND_URL, url);
    }
}
