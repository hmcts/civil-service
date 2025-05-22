package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimanthwfoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class NotifyClaimantHwFOutcomeHelperTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    private NotifyClaimantHwFOutcomeHelper helper;

    @BeforeEach
    void setUp() {
        openMocks(this);
        helper = new NotifyClaimantHwFOutcomeHelper(notificationsProperties);

        when(notificationsProperties.getNotifyApplicantForHwfInvalidRefNumber()).thenReturn("invalid-ref-template");
        when(notificationsProperties.getNotifyApplicantForHwfNoRemission()).thenReturn("no-remission-template");
        when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded()).thenReturn("more-info-template");
        when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber()).thenReturn("update-ref-template");
        when(notificationsProperties.getNotifyApplicantForHwfPartialRemission()).thenReturn("partial-remission-template");

        when(notificationsProperties.getNotifyApplicantForHwfInvalidRefNumberBilingual()).thenReturn("invalid-ref-template-bilingual");
        when(notificationsProperties.getNotifyApplicantForHwfNoRemissionWelsh()).thenReturn("no-remission-template-bilingual");
        when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeededWelsh()).thenReturn("more-info-template-bilingual");
        when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumberBilingual()).thenReturn("update-ref-template-bilingual");
        when(notificationsProperties.getNotifyApplicantForHwfPartialRemissionBilingual()).thenReturn("partial-remission-template-bilingual");
    }

    @Test
    void shouldReturnFurtherPropertiesForNoRemission() {
        CaseData caseData = CaseData.builder()
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                       .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET)
                                       .hwfCaseEvent(CaseEvent.NO_REMISSION_HWF)
                                       .outstandingFeeInPounds(new BigDecimal("1000"))
                                       .build())
            .build();

        Map<String, String> furtherProperties = helper.getFurtherProperties(caseData);

        assertThat(furtherProperties).containsEntry("reasons", "Fees requirement not met");
        assertThat(furtherProperties).containsEntry("reasonsWelsh", "Nid yw'r gofyniad ffioedd wedi'i fodloni");
        assertThat(furtherProperties).containsEntry("amount", "1000");
    }

    @Test
    void shouldReturnFurtherPropertiesForMoreInformation() {
        HelpWithFeesDetails hwfDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(CaseEvent.MORE_INFORMATION_HWF)
            .build();
        CaseData caseData = CaseData.builder()
            .claimIssuedHwfDetails(hwfDetails)
            .helpWithFeesMoreInformationClaimIssue(HelpWithFeesMoreInformation.builder()
                                                       .hwFMoreInfoDocumentDate(LocalDate.of(2023, 10, 1))
                                                       .hwFMoreInfoRequiredDocuments(List.of(HwFMoreInfoRequiredDocuments.CHILD_MAINTENANCE))
                                                       .build())
            .build();

        Map<String, String> furtherProperties = helper.getFurtherProperties(caseData);

        assertThat(furtherProperties).containsEntry("hwfMoreInfoDate", "01 October 2023");
        assertThat(furtherProperties).containsEntry("hwfMoreInfoDocuments", "Child maintenance\n\n");
        assertThat(furtherProperties).containsEntry("hwfMoreInfoDocumentsWelsh", "Cynnal plant\n\n");
    }

    @Test
    void shouldReturnFurtherPropertiesForPartialRemission() {
        HelpWithFeesDetails hwfDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED)
            .remissionAmount(new BigDecimal("500"))
            .outstandingFeeInPounds(new BigDecimal("100"))
            .build();
        CaseData caseData = CaseData.builder()
            .claimIssuedHwfDetails(hwfDetails)
            .build();

        Map<String, String> furtherProperties = helper.getFurtherProperties(caseData);

        assertThat(furtherProperties).containsEntry("partAmount", "500");
        assertThat(furtherProperties).containsEntry("remainingAmount", "100");
    }

    @Test
    void shouldThrowExceptionForInvalidCaseEvent() {
        CaseData caseData = CaseData.builder()
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                       .hwfCaseEvent(CaseEvent.CREATE_CLAIM)
                                       .build())
            .build();

        try {
            helper.getFurtherProperties(caseData);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("case event not found");
        }
    }
}
