package uk.gov.hmcts.reform.civil.notification.handlers.notifyhwfoutcomeparties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DOCUMENTS_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PART_AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REMAINING_AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASONS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASONS_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.TYPE_OF_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.TYPE_OF_FEE_WELSH;

class NotifyHwFOutcomePartiesHelperTest {

    private NotificationsProperties notificationsProperties;
    private NotifyHwFOutcomePartiesHelper helper;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        helper = new NotifyHwFOutcomePartiesHelper(notificationsProperties);
    }

    @Test
    void shouldReturnCorrectTemplateForCaseEvent() {
        when(notificationsProperties.getNotifyApplicantForHwfInvalidRefNumber()).thenReturn("template-invalid-ref");
        when(notificationsProperties.getNotifyApplicantForHwfNoRemission()).thenReturn("template-no-remission");
        when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded()).thenReturn("template-more-info");
        when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber()).thenReturn("template-update-ref");
        when(notificationsProperties.getNotifyApplicantForHwfPartialRemission()).thenReturn("template-partial-remission");

        assertThat(helper.getTemplate(CaseEvent.INVALID_HWF_REFERENCE)).isEqualTo("template-invalid-ref");
        assertThat(helper.getTemplate(CaseEvent.NO_REMISSION_HWF)).isEqualTo("template-no-remission");
        assertThat(helper.getTemplate(CaseEvent.MORE_INFORMATION_HWF)).isEqualTo("template-more-info");
        assertThat(helper.getTemplate(CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER)).isEqualTo("template-update-ref");
        assertThat(helper.getTemplate(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED)).isEqualTo("template-partial-remission");
    }

    @Test
    void shouldReturnCorrectBilingualTemplateForCaseEvent() {
        when(notificationsProperties.getNotifyApplicantForHwfInvalidRefNumberBilingual()).thenReturn("template-invalid-ref-bilingual");
        when(notificationsProperties.getNotifyApplicantForHwfNoRemissionWelsh()).thenReturn("template-no-remission-bilingual");
        when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeededWelsh()).thenReturn("template-more-info-bilingual");
        when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumberBilingual()).thenReturn("template-update-ref-bilingual");
        when(notificationsProperties.getNotifyApplicantForHwfPartialRemissionBilingual()).thenReturn("template-partial-remission-bilingual");

        assertThat(helper.getTemplateBilingual(CaseEvent.INVALID_HWF_REFERENCE)).isEqualTo("template-invalid-ref-bilingual");
        assertThat(helper.getTemplateBilingual(CaseEvent.NO_REMISSION_HWF)).isEqualTo("template-no-remission-bilingual");
        assertThat(helper.getTemplateBilingual(CaseEvent.MORE_INFORMATION_HWF)).isEqualTo("template-more-info-bilingual");
        assertThat(helper.getTemplateBilingual(CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER)).isEqualTo("template-update-ref-bilingual");
        assertThat(helper.getTemplateBilingual(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED)).isEqualTo("template-partial-remission-bilingual");
    }

    @Test
    void shouldReturnCommonProperties() {
        CaseData caseData = mock(CaseData.class);
        FeeType feeType = mock(FeeType.class); // Mock FeeType

        when(caseData.getLegacyCaseReference()).thenReturn("12345");
        when(caseData.getApplicant1()).thenReturn(mock(Party.class)); // Mock applicant1
        when(caseData.getHwfFeeType()).thenReturn(feeType); // Mock getHwfFeeType
        when(feeType.getLabel()).thenReturn("Fee Type");
        when(feeType.getLabelInWelsh()).thenReturn("Fee Type Welsh");
        when(caseData.getHwFReferenceNumber()).thenReturn("HWF123");

        try (MockedStatic<PartyUtils> mockedPartyUtils = mockStatic(PartyUtils.class)) {
            mockedPartyUtils.when(() -> PartyUtils.getPartyNameBasedOnType(caseData.getApplicant1()))
                .thenReturn("John Doe");

            Map<String, String> properties = helper.getCommonProperties(caseData);

            assertThat(properties).containsEntry(CLAIM_REFERENCE_NUMBER, "12345");
            assertThat(properties).containsEntry(CLAIMANT_NAME, "John Doe");
            assertThat(properties).containsEntry(TYPE_OF_FEE, "Fee Type");
            assertThat(properties).containsEntry(TYPE_OF_FEE_WELSH, "Fee Type Welsh");
            assertThat(properties).containsEntry(HWF_REFERENCE_NUMBER, "HWF123");
        }
    }

    @Test
    void shouldReturnFurtherPropertiesForNoRemission() {
        CaseData caseData = mock(CaseData.class);
        HelpWithFeesDetails helpWithFeesDetails = mock(HelpWithFeesDetails.class);

        when(caseData.getHwFEvent()).thenReturn(CaseEvent.NO_REMISSION_HWF);
        when(caseData.getHwFFeeAmount()).thenReturn(BigDecimal.valueOf(1000));
        when(caseData.isHWFTypeClaimIssued()).thenReturn(true);
        when(caseData.getClaimIssuedHwfDetails()).thenReturn(helpWithFeesDetails);
        when(helpWithFeesDetails.getNoRemissionDetailsSummary())
            .thenReturn(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET);

        Map<String, String> properties = helper.getFurtherProperties(caseData);

        assertThat(properties).containsEntry(REASONS, "Income/outgoings calculation determines Help with Fees requirement not met");
        assertThat(properties).containsEntry(REASONS_WELSH, "Mae’r cyfrifiad incwm/treuliau yn dangos nad yw’r gofyniad Help i Dalu Ffioedd wedi ei fodloni");
        assertThat(properties).containsEntry(AMOUNT, "1000");
    }

    @Test
    void shouldReturnFurtherPropertiesForMoreInformation() {
        CaseData caseData = mock(CaseData.class);
        HelpWithFeesMoreInformation moreInfo = mock(HelpWithFeesMoreInformation.class);

        when(caseData.getHwFEvent()).thenReturn(CaseEvent.MORE_INFORMATION_HWF);
        when(caseData.getHelpWithFeesMoreInformationClaimIssue()).thenReturn(moreInfo);
        when(moreInfo.getHwFMoreInfoDocumentDate()).thenReturn(LocalDate.of(2023, 1, 1));
        when(moreInfo.getHwFMoreInfoRequiredDocuments()).thenReturn(
            List.of(HwFMoreInfoRequiredDocuments.CHILD_MAINTENANCE)
        );

        Map<String, String> properties = helper.getFurtherProperties(caseData);

        assertThat(properties).containsEntry(HWF_MORE_INFO_DATE, "1 January 2023");
        String documentDetails = """
            Child maintenance - Evidence of being in receipt of Child Maintenance, \
            such as a Child Support Agency assessment, sealed court order or letter of agreement \
            showing how often and much you’re paid

            """;
        assertThat(properties).containsEntry(HWF_MORE_INFO_DOCUMENTS, documentDetails);
        String documentDetailsWelsh = """
            Cynhaliaeth plant - Tystiolaeth o dderbyn Cynhaliaeth Plant, \
            megis asesiad gan yr Asiantaeth Cynnal Plant, gorchymyn llys dan sêl; neu llythyr o gytundeb \
            yn dangos pa mor aml rydych yn cael eich a faint rydych yn cael eich talu

            """;
        assertThat(properties).containsEntry(HWF_MORE_INFO_DOCUMENTS_WELSH, documentDetailsWelsh);
    }

    @Test
    void shouldReturnFurtherPropertiesForPartialRemission() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED);
        when(caseData.getRemissionAmount()).thenReturn(BigDecimal.valueOf(500));
        when(caseData.getOutstandingFeeInPounds()).thenReturn(BigDecimal.valueOf(1000));

        Map<String, String> properties = helper.getFurtherProperties(caseData);

        assertThat(properties).containsEntry(PART_AMOUNT, "500");
        assertThat(properties).containsEntry(REMAINING_AMOUNT, "1000");
    }

    @Test
    void shouldNotIncludeDescriptionWhenEmptyInMoreInformationDocumentList() {
        HwFMoreInfoRequiredDocuments document = mock(HwFMoreInfoRequiredDocuments.class);
        when(document.getName()).thenReturn("Child maintenance");
        when(document.getDescription()).thenReturn("");

        List<HwFMoreInfoRequiredDocuments> documents = List.of(document);

        String result = helper.getMoreInformationDocumentList(documents);

        assertThat(result).isEqualTo("Child maintenance\n\n");
    }

    @Test
    void shouldNotIncludeDescriptionBilingualWhenEmptyInMoreInformationDocumentListWelsh() {
        HwFMoreInfoRequiredDocuments document = mock(HwFMoreInfoRequiredDocuments.class);
        when(document.getNameBilingual()).thenReturn("Cynhaliaeth plant");
        when(document.getDescriptionBilingual()).thenReturn(""); // Empty bilingual description

        List<HwFMoreInfoRequiredDocuments> documents = List.of(document);

        String result = helper.getMoreInformationDocumentListWelsh(documents);

        assertThat(result).isEqualTo("Cynhaliaeth plant\n\n"); // No bilingual description appended
    }

    @Test
    void shouldReturnNoRemissionReasonForHearingHWFType() {
        CaseData caseData = mock(CaseData.class);
        HelpWithFeesDetails hearingDetails = mock(HelpWithFeesDetails.class);

        when(caseData.isHWFTypeHearing()).thenReturn(true);
        when(caseData.getHearingHwfDetails()).thenReturn(hearingDetails);
        when(hearingDetails.getNoRemissionDetailsSummary()).thenReturn(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET);

        String result = helper.getHwFNoRemissionReason(caseData);

        assertThat(result).isEqualTo("Income/outgoings calculation determines Help with Fees requirement not met");
    }

    @Test
    void shouldReturnNoRemissionReasonForClaimIssuedHWFType() {
        CaseData caseData = mock(CaseData.class);
        HelpWithFeesDetails claimIssuedDetails = mock(HelpWithFeesDetails.class);

        when(caseData.isHWFTypeHearing()).thenReturn(false);
        when(caseData.isHWFTypeClaimIssued()).thenReturn(true);
        when(caseData.getClaimIssuedHwfDetails()).thenReturn(claimIssuedDetails);
        when(claimIssuedDetails.getNoRemissionDetailsSummary()).thenReturn(NoRemissionDetailsSummary.INCORRECT_EVIDENCE);

        String result = helper.getHwFNoRemissionReason(caseData);

        assertThat(result).isEqualTo("Incorrect evidence supplied");
    }

    @Test
    void shouldReturnEmptyStringWhenNoHWFTypeMatches() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isHWFTypeHearing()).thenReturn(false);
        when(caseData.isHWFTypeClaimIssued()).thenReturn(false);

        String result = helper.getHwFNoRemissionReason(caseData);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnNoRemissionReasonWelshForHearingHWFType() {
        CaseData caseData = mock(CaseData.class);
        HelpWithFeesDetails hearingDetails = mock(HelpWithFeesDetails.class);

        when(caseData.isHWFTypeHearing()).thenReturn(true);
        when(caseData.getHearingHwfDetails()).thenReturn(hearingDetails);
        when(hearingDetails.getNoRemissionDetailsSummary()).thenReturn(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET);

        String result = helper.getHwFNoRemissionReasonWelsh(caseData);

        assertThat(result).isEqualTo("Mae’r cyfrifiad incwm/treuliau yn dangos nad yw’r gofyniad Help i Dalu Ffioedd wedi ei fodloni");
    }

    @Test
    void shouldReturnNoRemissionReasonWelshForClaimIssuedHWFType() {
        CaseData caseData = mock(CaseData.class);
        HelpWithFeesDetails claimIssuedDetails = mock(HelpWithFeesDetails.class);

        when(caseData.isHWFTypeHearing()).thenReturn(false);
        when(caseData.isHWFTypeClaimIssued()).thenReturn(true);
        when(caseData.getClaimIssuedHwfDetails()).thenReturn(claimIssuedDetails);
        when(claimIssuedDetails.getNoRemissionDetailsSummary()).thenReturn(NoRemissionDetailsSummary.INCORRECT_EVIDENCE);

        String result = helper.getHwFNoRemissionReasonWelsh(caseData);

        assertThat(result).isEqualTo("Tystiolaeth anghywir wedi’i darparu");
    }

    @Test
    void shouldReturnEmptyStringForNoHWFTypeMatchesInWelsh() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isHWFTypeHearing()).thenReturn(false);
        when(caseData.isHWFTypeClaimIssued()).thenReturn(false);

        String result = helper.getHwFNoRemissionReasonWelsh(caseData);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnNullForUnsupportedCaseEventInTemplateMethods() {
        assertThat(helper.getTemplate(CaseEvent.REVIEW_HEARING_EXCEPTION)).isNull();
        assertThat(helper.getTemplateBilingual(CaseEvent.REVIEW_HEARING_EXCEPTION)).isNull();
    }

    @Test
    void shouldReturnEmptyMapForInvalidHwfReference() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.INVALID_HWF_REFERENCE);

        Map<String, String> properties = helper.getFurtherProperties(caseData);

        assertThat(properties).isEmpty();
    }

    @Test
    void shouldReturnEmptyMapForUpdateHelpWithFeeNumber() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER);

        Map<String, String> properties = helper.getFurtherProperties(caseData);

        assertThat(properties).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenHwFEventIsNull() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(null);

        assertThatThrownBy(() -> helper.getFurtherProperties(caseData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("HwFEvent is null in CaseData");
    }

    @Test
    void shouldThrowExceptionForUnsupportedCaseEvent() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.REVIEW_HEARING_EXCEPTION);

        assertThatThrownBy(() -> helper.getFurtherProperties(caseData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("case event not found");
    }

    @Test
    void shouldHandleNullHelpWithFeesMoreInformationClaimIssue() {
        CaseData caseData = mock(CaseData.class);
        HelpWithFeesMoreInformation moreInfo = mock(HelpWithFeesMoreInformation.class);

        when(caseData.getHwFEvent()).thenReturn(CaseEvent.MORE_INFORMATION_HWF);
        when(caseData.getHelpWithFeesMoreInformationClaimIssue()).thenReturn(null);
        when(caseData.getHelpWithFeesMoreInformationHearing()).thenReturn(moreInfo);
        when(moreInfo.getHwFMoreInfoDocumentDate()).thenReturn(LocalDate.of(2023, 1, 1));
        when(moreInfo.getHwFMoreInfoRequiredDocuments()).thenReturn(
            List.of(HwFMoreInfoRequiredDocuments.CHILD_MAINTENANCE)
        );

        Map<String, String> properties = helper.getFurtherProperties(caseData);

        assertThat(properties).containsEntry(HWF_MORE_INFO_DATE, "1 January 2023");
        String moreInfoDocumentDetails = """
            Child maintenance - Evidence of being in receipt of Child Maintenance, \
            such as a Child Support Agency assessment, sealed court order or letter of agreement \
            showing how often and much you’re paid

            """;
        assertThat(properties).containsEntry(HWF_MORE_INFO_DOCUMENTS, moreInfoDocumentDetails);
        String moreInfoDocumentDetailsWelsh = """
            Cynhaliaeth plant - Tystiolaeth o dderbyn Cynhaliaeth Plant, \
            megis asesiad gan yr Asiantaeth Cynnal Plant, gorchymyn llys dan sêl; neu llythyr o gytundeb \
            yn dangos pa mor aml rydych yn cael eich a faint rydych yn cael eich talu

            """;
        assertThat(properties).containsEntry(HWF_MORE_INFO_DOCUMENTS_WELSH, moreInfoDocumentDetailsWelsh);
    }
}
