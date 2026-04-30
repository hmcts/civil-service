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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_EMAIL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_EMAIL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;

@ExtendWith(MockitoExtension.class)
class GenerateSpecDJFormRequestedRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String REFERENCE_TEMPLATE = "default-judgment-respondent-requested-notification-%s";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    private GenerateSpecDJFormRequestedRespSolTwoEmailDTOGenerator generator;
    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;
    private MockedStatic<PartyUtils> partyUtilsMockedStatic;
    private GenerateSpecDJFormNotificationHelper notificationHelper;

    @BeforeEach
    void setUp() {
        notificationHelper = new GenerateSpecDJFormNotificationHelper();
        generator = new GenerateSpecDJFormRequestedRespSolTwoEmailDTOGenerator(organisationService, notificationsProperties,
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
        when(notificationsProperties.getRespondentSolicitor1DefaultJudgmentRequested()).thenReturn(TEMPLATE_ID);

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
        Party applicant1 = mock(Party.class);
        DynamicListElement selectedElement = DynamicListElement.dynamicElementFromCode("code", "Second Defendant");
        DynamicList list = new DynamicList(selectedElement, List.of(selectedElement));

        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getDefendantDetailsSpec()).thenReturn(list);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
            .thenReturn("Respondent Two Org");
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn("Applicant Org");
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(applicant1)).thenReturn("Applicant 1");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(DEFENDANT_EMAIL, "Respondent Two Org");
        assertThat(result).containsEntry(CLAIMANT_EMAIL, "Applicant Org");
        assertThat(result).containsEntry(CLAIM_NUMBER, "1234567890123456");
        assertThat(result).containsEntry(DEFENDANT_NAME, "Second Defendant");
        assertThat(result).containsEntry(CLAIMANT_NAME, "Applicant 1");
    }

    @Test
    void shouldFallbackToRespondentTwoNameWhenDynamicListMissing() {
        Map<String, String> properties = new HashMap<>();
        CaseData caseData = mock(CaseData.class);
        Party applicant1 = mock(Party.class);
        Party respondent2 = mock(Party.class);

        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getDefendantDetailsSpec()).thenReturn(null);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
            .thenReturn("Respondent Two Org");
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn("Applicant Org");
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(applicant1)).thenReturn("Applicant 1");
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(respondent2)).thenReturn("Respondent 2");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(DEFENDANT_NAME, "Respondent 2");
    }

    @Test
    void shouldNotifyWhenOnlySecondDefendantSelectedAndTwoSolicitors() {
        CaseData baseCaseData = multiPartyCaseData();
        Party respondent2 = baseCaseData.getRespondent2();

        CaseData caseData = multiPartyCaseData(DynamicListElement.dynamicElementFromCode("second", respondent2.getPartyName()));

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyWhenFirstDefendantSelectedInstead() {
        CaseData baseCaseData = multiPartyCaseData();
        String firstDefendantName = baseCaseData.getRespondent1().getPartyName();

        CaseData caseData = multiPartyCaseData(DynamicListElement.dynamicElementFromCode("first", firstDefendantName));

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenBothDefendantsSelected() {
        CaseData caseData = multiPartyCaseData(DynamicListElement.dynamicElementFromCode("both", "Both Defendants"));

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenCaseHasSingleDefendant() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    private CaseData multiPartyCaseData() {
        return multiPartyCaseData(null);
    }

    private CaseData multiPartyCaseData(DynamicListElement selectedDefendant) {
        Party respondent1 = new PartyBuilder().individual().build();
        Party respondent2 = new PartyBuilder().individual("Second").build();
        DynamicListElement value = selectedDefendant != null
            ? selectedDefendant
            : DynamicListElement.dynamicElementFromCode("second", respondent2.getPartyName());
        CaseData baseCaseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .addRespondent2(YesOrNo.YES)
            .build();
        baseCaseData.setDefendantDetailsSpec(new DynamicList(value, List.of(value)));
        return baseCaseData;
    }
}
