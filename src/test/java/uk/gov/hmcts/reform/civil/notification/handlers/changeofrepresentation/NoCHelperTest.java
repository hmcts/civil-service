package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.COURT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;

@ExtendWith(MockitoExtension.class)
class NoCHelperTest {

    private static final String APP_SOLICITOR_EMAIL = "appSol@gmail.com";

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private NoCHelper noCHelper;

    @Test
    void shouldReturnCorrectPropertiesForGetProperties() {
        String orgToRemoveId = "remove-id";
        String orgToAddId = "add-id";

        Organisation orgToRemove = Organisation.builder()
            .name("remove org")
            .build();

        Organisation orgToAdd = Organisation.builder()
            .name("add org")
            .build();

        when(organisationService.findOrganisationById(orgToRemoveId)).thenReturn(Optional.of(orgToRemove));
        when(organisationService.findOrganisationById(orgToAddId)).thenReturn(Optional.of(orgToAdd));

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualLastName("Doe").individualFirstName("John").build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .email(APP_SOLICITOR_EMAIL).build())
            .respondent1(Party.builder().partyName("John Doe").type(Party.Type.INDIVIDUAL)
                             .individualFirstName("John")
                             .individualLastName("Doe")
                             .build())
            .issueDate(LocalDate.of(2023, 1, 10))
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                        .organisationToRemoveID(orgToRemoveId)
                                        .organisationToAddID(orgToAddId)
                                        .build())
            .build();

        Map<String, String> result = noCHelper.getProperties(caseData, false);

        assertThat(result).containsEntry(FORMER_SOL, "remove org");
        assertThat(result).containsEntry(NEW_SOL, "add org");
    }

    @Test
    void shouldReturnLipWhenOrganisationIdIsNull() {
        String orgToAddId = "add-id";

        Organisation orgToAdd = Organisation.builder()
            .name("add org")
            .build();

        when(organisationService.findOrganisationById(orgToAddId)).thenReturn(Optional.of(orgToAdd));

        Map<String, String> result = noCHelper.getProperties(
            CaseData.builder()
                .ccdCaseReference(1234567890123456L)
                .issueDate(LocalDate.now())
                .applicant1(Party.builder().partyName("Jane Doe").type(Party.Type.INDIVIDUAL)
                                .individualFirstName("Jane")
                                .individualLastName("Doe")
                                .build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .email(APP_SOLICITOR_EMAIL).build())
                .respondent1(Party.builder().partyName("John Doe").type(Party.Type.INDIVIDUAL)
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .build())
                .changeOfRepresentation(ChangeOfRepresentation.builder()
                                            .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                            .organisationToAddID(orgToAddId)
                                            .organisationToRemoveID(null)
                                            .build())
                .build(), false);

        assertThat(result).containsEntry(FORMER_SOL, "LiP");
        assertThat(result).containsEntry(NEW_SOL, "add org");
    }

    @Test
    void shouldThrowExceptionWhenOrganisationNotFound() {
        String orgId = "not-found";
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
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualLastName("A")
                            .individualFirstName("Claimant")
                            .partyName("Claimant A").build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualLastName("B")
                             .individualFirstName("Defendant")
                             .partyName("Defendant B").build())
            .ccdCaseReference(1234567890123456L)
            .legacyCaseReference("LEGACY123")
            .build();

        Map<String, String> props = noCHelper.getClaimantLipProperties(caseData);
        assertThat(props).containsEntry(CLAIMANT_NAME, "Claimant A");
        assertThat(props).containsEntry(CLAIM_NUMBER, "LEGACY123");
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
        assertThat(props).containsEntry(HEARING_DATE, "1 October 2023");
        assertThat(props).containsEntry(COURT_LOCATION, "Courtroom A");
        assertThat(props).containsEntry(HEARING_FEE, "£100.00");
    }
}
