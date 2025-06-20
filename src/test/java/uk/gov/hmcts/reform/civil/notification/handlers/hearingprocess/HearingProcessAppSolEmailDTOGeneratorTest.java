package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getAppSolReference;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getHearingFeePropertiesIfNotPaid;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.isNoFeeDue;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingTime;

@ExtendWith(MockitoExtension.class)
public class HearingProcessAppSolEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private HearingProcessAppSolEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnNoFeeTemplateWhenHearingFeeStatusIsSuccess() {
        PaymentDetails paymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .build();
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(paymentDetails)
            .build();
        String expectedTemplateId = "template-id";

        MockedStatic<HearingProcessHelper> hearingProcessHelperMockedStatic = Mockito.mockStatic(HearingProcessHelper.class);
        hearingProcessHelperMockedStatic.when(() -> isNoFeeDue(caseData))
            .thenReturn(true);
        when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        hearingProcessHelperMockedStatic.close();

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnNoFeeTemplateWhenHearingNoticeListIsOther() {
        CaseData caseData = CaseData.builder()
            .hearingNoticeList(HearingNoticeList.OTHER)
            .build();
        String expectedTemplateId = "template-id";

        MockedStatic<HearingProcessHelper> hearingProcessHelperMockedStatic = Mockito.mockStatic(HearingProcessHelper.class);
        hearingProcessHelperMockedStatic.when(() -> isNoFeeDue(caseData))
            .thenReturn(true);
        when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        hearingProcessHelperMockedStatic.close();

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnNoFeeTemplateWhenListingOrRelistingIsRelisting() {
        CaseData caseData = CaseData.builder()
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .build();

        String expectedTemplateId = "template-id";
        MockedStatic<HearingProcessHelper> hearingProcessHelperMockedStatic = Mockito.mockStatic(HearingProcessHelper.class);
        hearingProcessHelperMockedStatic.when(() -> isNoFeeDue(caseData))
            .thenReturn(true);
        when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        hearingProcessHelperMockedStatic.close();

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnFeeTemplateWhenNoNoFeeConditionsMet() {
        CaseData caseData = CaseData.builder().build();

        String expectedTemplateId = "template-id";
        MockedStatic<HearingProcessHelper> hearingProcessHelperMockedStatic = Mockito.mockStatic(HearingProcessHelper.class);
        hearingProcessHelperMockedStatic.when(() -> isNoFeeDue(caseData))
            .thenReturn(false);
        when(notificationsProperties.getHearingListedFeeClaimantLrTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        hearingProcessHelperMockedStatic.close();

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
        SolicitorReferences solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference("REF123")
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDate(LocalDate.parse("2025-07-01"))
            .hearingTimeHourMinute("10:30")
            .applicant1(party)
            .applicant1OrganisationPolicy(organisationPolicy)
            .solicitorReferences(solicitorReferences)
            .build();

        try (
            MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
            MockedStatic<HearingProcessHelper> hearingProcessHelperMockedStatic = Mockito.mockStatic(HearingProcessHelper.class)
        ) {
            notificationUtilsMockedStatic.when(() -> getFormattedHearingDate(LocalDate.parse("2025-07-01")))
                .thenReturn("1 July 2025");
            notificationUtilsMockedStatic.when(() -> getFormattedHearingTime("10:30"))
                .thenReturn("10:30 AM");
            notificationUtilsMockedStatic.when(() -> getApplicantLegalOrganizationName(caseData, organisationService))
                .thenReturn("Organization Name");

            Map<String, String> hearingFeeProperties = new HashMap<>();
            hearingFeeProperties.put(HEARING_FEE, "£200");
            hearingFeeProperties.put(HEARING_DUE_DATE, "1 June 2025");

            hearingProcessHelperMockedStatic.when(() -> getHearingFeePropertiesIfNotPaid(caseData))
                .thenReturn(hearingFeeProperties);
            hearingProcessHelperMockedStatic.when(() -> getAppSolReference(caseData))
                .thenReturn("REF123");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

            assertThat(updatedProperties)
                .containsEntry(HEARING_DATE, "1 July 2025")
                .containsEntry(HEARING_TIME, "10:30 AM")
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Organization Name")
                .containsEntry(CLAIMANT_REFERENCE_NUMBER, "REF123")
                .containsEntry(HEARING_FEE, "£200")
                .containsEntry(HEARING_DUE_DATE, "1 June 2025")
                .hasSize(6);
        }
    }
}
