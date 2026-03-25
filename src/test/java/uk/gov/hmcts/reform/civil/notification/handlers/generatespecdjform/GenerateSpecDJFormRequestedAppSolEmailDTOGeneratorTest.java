package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_APPLICANT1;

@ExtendWith(MockitoExtension.class)
class GenerateSpecDJFormRequestedAppSolEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String REFERENCE_TEMPLATE = "default-judgment-applicant-requested-notification-%s";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    private GenerateSpecDJFormRequestedAppSolEmailDTOGenerator generator;
    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;
    private MockedStatic<PartyUtils> partyUtilsMockedStatic;
    private GenerateSpecDJFormNotificationHelper notificationHelper;

    @BeforeEach
    void setUp() {
        notificationHelper = new GenerateSpecDJFormNotificationHelper();
        generator = new GenerateSpecDJFormRequestedAppSolEmailDTOGenerator(organisationService, notificationsProperties,
            notificationHelper);
        notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
        partyUtilsMockedStatic = mockStatic(PartyUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (notificationUtilsMockedStatic != null) {
            notificationUtilsMockedStatic.close();
        }
        if (partyUtilsMockedStatic != null) {
            partyUtilsMockedStatic.close();
        }
    }

    @Test
    void shouldReturnTemplateId() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getApplicantSolicitor1DefaultJudgmentRequested()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomPropertiesUsingDynamicList() {
        Map<String, String> properties = new HashMap<>();
        CaseData caseData = mock(CaseData.class);
        DynamicListElement element = DynamicListElement.dynamicElementFromCode("code", "Both Defendants");
        DynamicList list = new DynamicList(element, List.of(element));

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn("Applicant Org");
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getDefendantDetailsSpec()).thenReturn(list);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(LEGAL_ORG_APPLICANT1, "Applicant Org");
        assertThat(result).containsEntry(CLAIM_NUMBER, "1234567890123456");
        assertThat(result).containsEntry(DEFENDANT_NAME, "Both Defendants");
    }

    @Test
    void shouldFallbackToRespondentNameWhenNoDynamicList() {
        Map<String, String> properties = new HashMap<>();
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn("Applicant Org");
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getDefendantDetailsSpec()).thenReturn(null);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(respondent1)).thenReturn("Respondent 1");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(DEFENDANT_NAME, "Respondent 1");
    }

    @Test
    void shouldReturnTrueWhenOnlyOneDefendantSelected() {
        DynamicListElement firstDefendant = DynamicListElement.dynamicElement("First Defendant");
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent2(new PartyBuilder().individual().build())
            .addRespondent2(YesOrNo.YES)
            .build();
        caseData.setDefendantDetailsSpec(new DynamicList(firstDefendant, List.of(firstDefendant)));

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenBothDefendantsSelected() {
        DynamicListElement bothDefendants = DynamicListElement.dynamicElement("Both Defendants");
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent2(new PartyBuilder().individual().build())
            .addRespondent2(YesOrNo.YES)
            .build();
        caseData.setDefendantDetailsSpec(new DynamicList(bothDefendants, List.of(bothDefendants)));

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }
}
