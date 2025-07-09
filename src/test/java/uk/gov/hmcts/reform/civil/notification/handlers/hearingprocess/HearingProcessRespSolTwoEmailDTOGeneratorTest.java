package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getRespSolTwoReference;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingTime;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@ExtendWith(MockitoExtension.class)
public class HearingProcessRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private HearingProcessRespSolTwoEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("notification-of-hearing-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Party party = Party.builder().build();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().build();
        CaseData caseData = CaseData.builder()
            .hearingDate(LocalDate.parse("2025-07-01"))
            .hearingTimeHourMinute("10:30")
            .respondent2(party)
            .respondent2OrganisationPolicy(organisationPolicy)
            .build();

        try (
            MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
            MockedStatic<HearingProcessHelper> hearingProcessHelperMockedStatic = Mockito.mockStatic(HearingProcessHelper.class)
        ) {
            notificationUtilsMockedStatic.when(() -> getFormattedHearingDate(LocalDate.parse("2025-07-01")))
                .thenReturn("1 July 2025");
            notificationUtilsMockedStatic.when(() -> getFormattedHearingTime("10:30"))
                .thenReturn("10:30 AM");
            notificationUtilsMockedStatic.when(() -> getRespondentLegalOrganizationName(organisationPolicy, organisationService))
                .thenReturn("Organization Name");

            hearingProcessHelperMockedStatic.when(() -> getRespSolTwoReference(caseData))
                .thenReturn("REF123");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

            assertThat(updatedProperties)
                .containsEntry(HEARING_DATE, "1 July 2025")
                .containsEntry(HEARING_TIME, "10:30 AM")
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Organization Name")
                .containsEntry(DEFENDANT_REFERENCE_NUMBER, "REF123")
                .hasSize(4);
        }
    }
}
