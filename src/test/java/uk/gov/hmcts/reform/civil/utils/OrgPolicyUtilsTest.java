package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrgPolicyUtilsTest {

    @Nested
    class GetRespondent1SolicitorOrgId {
        @Test
        void shouldReturnNull_whenRespondent1OrganisationPolicyDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder().build();
            assertNull(OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnNull_whenRespondent1OrganisationPolicyOrganisationDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .build();

            assertNull(OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnOrganisationId_whenRespondent1OrganisationPolicyOrganisationExist() {
            String expected = "original-id";
            CaseData caseData = CaseDataBuilder.builder().respondent1OrganisationPolicy(
                    OrganisationPolicy.builder().organisation(
                        Organisation.builder().organisationID(expected).build()).build())
                .build();

            assertEquals(expected, OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnOrganisationIdCopy_whenRespondent1OrganisationPolicyOrganisationNotExist() {
            String expected = "copy-id";
            CaseData caseData = CaseDataBuilder.builder().respondent1OrganisationIDCopy(expected).build();

            assertEquals(expected, OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnOrganisationId_whenBothRespondent1OrganisationPolicyAndCopyExist() {
            String expected = "original-id";
            CaseData caseData = CaseDataBuilder.builder().respondent1OrganisationPolicy(
                    OrganisationPolicy.builder().organisation(Organisation.builder()
                            .organisationID(expected).build()).build()).respondent1OrganisationIDCopy(expected)
                .build();

            assertEquals(expected, OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData));
        }
    }

    @Nested
    class GetRespondent2SolicitorOrgId {
        @Test
        void shouldReturnNull_whenRespondent2OrganisationPolicyDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder().build();
            assertNull(OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnNull_whenRespondent2OrganisationPolicyOrganisationDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
                .build();

            assertNull(OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnOrganisationId_whenRespondent2OrganisationPolicyOrganisationExist() {
            String expected = "original-id";
            CaseData caseData = CaseDataBuilder.builder().respondent2OrganisationPolicy(
                    OrganisationPolicy.builder().organisation(
                        Organisation.builder().organisationID(expected).build()).build()).build();

            assertEquals(expected, OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnOrganisationIdCopy_whenRespondent2OrganisationPolicyOrganisationNotExist() {
            String expected = "copy-id";
            CaseData caseData = CaseDataBuilder.builder().respondent2OrganisationIDCopy(expected).build();

            assertEquals(expected, OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnOrganisationId_whenBothRespondent2OrganisationPolicyAndCopyExist() {
            String expected = "original-id";
            CaseData caseData = CaseDataBuilder.builder().respondent2OrganisationPolicy(
                    OrganisationPolicy.builder().organisation(Organisation.builder().organisationID(expected).build())
                        .build()).respondent2OrganisationIDCopy(expected).build();

            assertEquals(expected, OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData));
        }
    }

    @Nested
    class AddMissingOrgPolicies {
        @Test
        void shouldPopulateGivenCaseDataBuilderWithExpectedRespondentOrgPolicies_whenBothDoNotExist() {
            var caseDataBuilder = CaseData.builder();

            OrgPolicyUtils.addMissingOrgPolicies(caseDataBuilder);
            var actual = caseDataBuilder.build();

            assertEquals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName(),
                         actual.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole());
            assertEquals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName(),
                         actual.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }

        @Test
        void shouldNotOverwriteRespondent1OrgPolicy_whenItExists() {
            var caseDataBuilder =
                CaseData.builder().respondent1OrganisationPolicy(
                    OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[RES1-EXISTING]").build());

            OrgPolicyUtils.addMissingOrgPolicies(caseDataBuilder);
            var actual = caseDataBuilder.build();

            assertEquals("[RES1-EXISTING]",
                         actual.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole());
            assertEquals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName(),
                         actual.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }

        @Test
        void shouldNotOverwriteRespondent2OrgPolicy_whenItExists() {
            var caseDataBuilder =
                CaseData.builder().respondent2OrganisationPolicy(
                    OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[RES2-EXISTING]").build());

            OrgPolicyUtils.addMissingOrgPolicies(caseDataBuilder);
            var actual = caseDataBuilder.build();

            assertEquals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName(),
                         actual.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole());
            assertEquals("[RES2-EXISTING]",
                         actual.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }

        @Test
        void shouldNotOverwriteRespondentOrgPolicies_whenBothExist() {
            var caseDataBuilder =
                CaseData.builder()
                    .respondent1OrganisationPolicy(
                        OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[RES1-EXISTING]").build())
                    .respondent2OrganisationPolicy(
                        OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[RES2-EXISTING]").build());

            OrgPolicyUtils.addMissingOrgPolicies(caseDataBuilder);
            var actual = caseDataBuilder.build();

            assertEquals("[RES1-EXISTING]",
                         actual.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole());
            assertEquals("[RES2-EXISTING]",
                         actual.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }
    }
}
