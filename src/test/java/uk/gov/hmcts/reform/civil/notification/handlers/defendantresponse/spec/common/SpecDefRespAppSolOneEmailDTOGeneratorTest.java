package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;

class SpecDefRespAppSolOneEmailDTOGeneratorTest {

    private SpecDefRespAppSolOneEmailDTOGenerator generator;
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        SpecDefRespEmailHelper specDefRespEmailHelper = mock(SpecDefRespEmailHelper.class);
        organisationService = mock(OrganisationService.class);
        generator = new SpecDefRespAppSolOneEmailDTOGenerator(specDefRespEmailHelper, organisationService);
    }

    @Test
    void shouldAddWhenWillBePaidImmediatelyWhenFullAdmissionAndImmediatePayment() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.of(2025, Month.MAY, 10))
            )
            .build();

        String expectedOrgName = "OrgName Ltd";
        Map<String, String> properties = new HashMap<>();
        try (var mockedUtils = mockStatic(uk.gov.hmcts.reform.civil.utils.NotificationUtils.class)) {

            mockedUtils.when(() ->
                                 uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName(
                                     caseData,
                                     organisationService
                                 )
            ).thenReturn(expectedOrgName);

            Map<String, String> result = generator.addCustomProperties(properties, caseData);

            assertThat(result).containsEntry("payImmediately", "10 MAY 2025")
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, expectedOrgName);
        }
    }

    @Test
    void shouldAddClaimantNameWhenNotImmediatePayment() {
        Party applicant = Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualFirstName("Jane")
            .individualLastName("Doe").build();
        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .defenceAdmitPartPaymentTimeRouteRequired(null)
            .build();

        Map<String, String> properties = new HashMap<>();
        String expectedOrgName = "OrgName Ltd";
        try (var mockedUtils = mockStatic(uk.gov.hmcts.reform.civil.utils.NotificationUtils.class)) {

            mockedUtils.when(() ->
                                 uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName(
                                     caseData,
                                     organisationService
                                 )
            ).thenReturn(expectedOrgName);

            Map<String, String> result = generator.addCustomProperties(properties, caseData);

            assertThat(result)
                .containsEntry(CLAIMANT_NAME, "Jane Doe")
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, expectedOrgName);
        }
    }
}
