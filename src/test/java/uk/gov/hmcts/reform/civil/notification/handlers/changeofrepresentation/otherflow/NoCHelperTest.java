package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.COURT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME_WITH_SPACE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REFERENCE;

@ExtendWith(MockitoExtension.class)
class NoCHelperTest {

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private NoCHelper noCHelper;

    private CaseData baseCaseData;

    @BeforeEach
    void setUp() {
        baseCaseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("Applicant")
                            .individualLastName("A")
                            .partyName("Applicant A").build())
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("QWERTY A")))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualFirstName("Respondent")
                             .individualLastName("A")
                             .partyName("Respondent A").build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualFirstName("Respondent")
                             .individualLastName("B")
                             .partyName("Respondent B").build())
            .legacyCaseReference("LEGACY-REF")
            .issueDate(LocalDate.of(2024, 5, 1))
            .changeOfRepresentation(new ChangeOfRepresentation()
                                        .setCaseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                        .setOrganisationToAddID("orgAdd")
                                        .setOrganisationToRemoveID("orgRemove")
                                        .setFormerRepresentationEmailAddress("former@sol.com"))
            .applicant1Represented(YesOrNo.YES)
            .hearingDate(LocalDate.of(2024, 6, 1))
            .hearingDueDate(LocalDate.of(2024, 5, 20))
            .hearingTimeHourMinute("10:30")
            .hearingFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(10000)))
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("Court A").build()).build())
            .hearingFeePaymentDetails(new PaymentDetails().setStatus(PaymentStatus.SUCCESS))
            .build();
    }

    @Test
    void getProperties_shouldReturnExpectedMap() {

        when(organisationService.findOrganisationById("QWERTY A"))
            .thenReturn(Optional.of(new Organisation().setName("App Legal Org")));

        when(organisationService.findOrganisationById("orgAdd"))
            .thenReturn(Optional.of(new Organisation().setName("New Org")));

        when(organisationService.findOrganisationById("orgRemove"))
            .thenReturn(Optional.of(new Organisation().setName("Old Org")));

        Map<String, String> props = noCHelper.getProperties(baseCaseData, false);

        assertThat(props)
            .containsEntry(CASE_NAME, "Applicant A v Respondent A, Respondent B")
            .containsEntry(ISSUE_DATE, "1 May 2024")
            .containsEntry(CCD_REF, "1234567890123456")
            .containsEntry(FORMER_SOL, "Old Org")
            .containsEntry(NEW_SOL, "New Org")
            .containsEntry(OTHER_SOL_NAME, "App Legal Org")
            .containsEntry(LEGAL_REP_NAME_WITH_SPACE, "New Org")
            .containsEntry(REFERENCE, "1234567890123456");
    }

    @Test
    void getClaimantLipProperties_shouldReturnExpectedMap() {
        Map<String, String> props = noCHelper.getClaimantLipProperties(baseCaseData);

        assertThat(props)
            .containsEntry(CLAIMANT_NAME, "Applicant A")
            .containsEntry(DEFENDANT_NAME_INTERIM, "Respondent A")
            .containsEntry(CLAIM_NUMBER, "LEGACY-REF")
            .containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567890123456");
    }

    @Test
    void getHearingFeeEmailProperties_shouldReturnExpectedMap() {
        when(organisationService.findOrganisationById("QWERTY A"))
            .thenReturn(Optional.of(new Organisation().setName("App Legal Org")));

        Map<String, String> props = noCHelper.getHearingFeeEmailProperties(baseCaseData);

        assertThat(props)
            .containsEntry(LEGAL_ORG_NAME, "App Legal Org")
            .containsEntry(HEARING_DATE, "1 June 2024")
            .containsEntry(COURT_LOCATION, "Court A")
            .containsEntry(HEARING_TIME, "10:30")
            .containsEntry(HEARING_FEE, "£100.00")
            .containsEntry(HEARING_DUE_DATE, "20 May 2024");
    }

    @Test
    void isHearingFeePaid_shouldReturnTrueWhenSuccess() {
        assertTrue(noCHelper.isHearingFeePaid(baseCaseData));
    }
}
