package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.COURT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;

class NoCHelperTest {

    private OrganisationService organisationService;
    private NoCHelper noCHelper;

    @BeforeEach
    void setUp() {
        organisationService = mock(OrganisationService.class);
        noCHelper = new NoCHelper(organisationService);
    }

    @Test
    void shouldReturnCorrectPropertiesForGetProperties() {
        String orgToRemoveId = "remove-id";
        String orgToAddId = "add-id";

        Organisation ORGANISATION_TO_REMOVE = Organisation.builder()
            .name("remove org")
            .build();

        Organisation ORGANISATION_TO_ADD = Organisation.builder()
            .name("add org")
            .build();

        when(organisationService.findOrganisationById(orgToRemoveId)).thenReturn(Optional.of(ORGANISATION_TO_REMOVE));
        when(organisationService.findOrganisationById(orgToAddId)).thenReturn(Optional.of(ORGANISATION_TO_ADD));

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualLastName("Doe").individualFirstName("John").build())
            .issueDate(LocalDate.of(2023, 1, 10))
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .organisationToRemoveID(orgToRemoveId)
                                        .organisationToAddID(orgToAddId)
                                        .build())
            .build();

        Map<String, String> result = noCHelper.getProperties(caseData, false);

        assertThat(result.get(FORMER_SOL)).isEqualTo("Old Firm");
        assertThat(result.get(NEW_SOL)).isEqualTo("New Firm");
    }

    @Test
    void shouldReturnLipWhenOrganisationIdIsNull() {
        Map<String, String> result = noCHelper.getProperties(
            CaseData.builder()
                .ccdCaseReference(1234567890123456L)
                .issueDate(LocalDate.now())
                .applicant1(Party.builder().partyName("Jane Doe").type(Party.Type.INDIVIDUAL)
                                .individualFirstName("Jane")
                                .individualLastName("Doe")
                                .build())
                .respondent1(Party.builder().partyName("John Doe").type(Party.Type.INDIVIDUAL)
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .build())
                .changeOfRepresentation(ChangeOfRepresentation.builder()
                                            .organisationToAddID(null)
                                            .organisationToRemoveID(null)
                                            .build())
                .build(), false);

        assertThat(result.get(FORMER_SOL)).isEqualTo("LiP");
        assertThat(result.get(NEW_SOL)).isEqualTo("LiP");
    }

    @Test
    void shouldThrowExceptionWhenOrganisationNotFound() {
        String orgId = "not-found";
        when(organisationService.findOrganisationById(orgId)).thenReturn(Optional.empty());

        CaseData caseData = CaseData.builder()
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .organisationToRemoveID(orgId)
                                        .build())
            .ccdCaseReference(1234567890123456L)
            .issueDate(LocalDate.now())
            .build();

        assertThrows(RuntimeException.class, () -> noCHelper.getProperties(caseData, false));
    }

    @Test
    void shouldReturnCorrectLipClaimantProperties() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().partyName("Claimant A").build())
            .respondent1(Party.builder().partyName("Defendant B").build())
            .ccdCaseReference(1234567890123456L)
            .legacyCaseReference("LEGACY123")
            .build();

        Map<String, String> props = noCHelper.getClaimantLipProperties(caseData);
        assertThat(props.get(CLAIMANT_NAME)).isEqualTo("Claimant A");
        assertThat(props.get(CLAIM_NUMBER)).isEqualTo("LEGACY123");
    }

    @Test
    void shouldReturnTrueWhenHearingFeePaid() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
            .build();

        assertThat(noCHelper.isHearingFeePaid(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenHearingFeeNotPaid() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.FAILED).build())
            .build();

        assertThat(noCHelper.isHearingFeePaid(caseData)).isFalse();
    }

    @Test
    void shouldReturnCorrectHearingFeeEmailProperties() {
        Organisation org = Organisation.builder()
                .name("app sol org")
                .build();
        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(org));

        CaseData caseData = CaseData.builder()
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                .organisationID("id")
                                                                .build())
                                              .build())
            .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder().name("Fallback Name").build())
            .hearingDate(LocalDate.of(2023, 10, 1))
            .hearingDueDate(LocalDate.of(2023, 9, 1))
            .hearingLocation(DynamicList.builder()
                                 .value(DynamicListElement.builder().label("Courtroom A").build())
                                 .build())
            .hearingTimeHourMinute("10:00 AM")
            .hearingFee(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal("10000"))
                            .build())
            .build();

        Map<String, String> props = noCHelper.getHearingFeeEmailProperties(caseData);
        assertThat(props.get(HEARING_DATE)).isEqualTo("01 Oct 2023");
        assertThat(props.get(COURT_LOCATION)).isEqualTo("Courtroom A");
        assertThat(props.get(HEARING_FEE)).isEqualTo("10000");
    }
}
