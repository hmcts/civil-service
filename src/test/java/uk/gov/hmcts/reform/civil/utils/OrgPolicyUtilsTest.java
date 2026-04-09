package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisation;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisationCollectionItem;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;
import java.util.List;

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
                .respondent1OrganisationPolicy(new OrganisationPolicy())
                .build();

            assertNull(OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnOrganisationId_whenRespondent1OrganisationPolicyOrganisationExist() {
            String expected = "original-id";
            CaseData caseData = CaseDataBuilder.builder().respondent1OrganisationPolicy(
                    new OrganisationPolicy().setOrganisation(
                        new Organisation().setOrganisationID(expected)))
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
                    new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID(expected))).respondent1OrganisationIDCopy(expected)
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
                .respondent2OrganisationPolicy(new OrganisationPolicy())
                .build();

            assertNull(OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData));
        }

        @Test
        void shouldReturnOrganisationId_whenRespondent2OrganisationPolicyOrganisationExist() {
            String expected = "original-id";
            CaseData caseData = CaseDataBuilder.builder().respondent2OrganisationPolicy(
                    new OrganisationPolicy().setOrganisation(
                        new Organisation().setOrganisationID(expected))).build();

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
                    new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID(expected))).respondent2OrganisationIDCopy(expected).build();

            assertEquals(expected, OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData));
        }
    }

    @Nested
    class BuildPreviousOrganisationTest {
        @Test
        void shouldGetLatestOrganisationChange_whenChangesExist() {
            var latestDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
            var oldestDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");

            var orgPolicy = new OrganisationPolicy().setPreviousOrganisations(List.of(
                    buildPreviousOrganisation("latest-previous-org", latestDate),
                    buildPreviousOrganisation("oldest-previous-org", oldestDate)));

            var actual = OrgPolicyUtils.getLatestOrganisationChanges(orgPolicy);
            PreviousOrganisation expected = new PreviousOrganisation();
            expected.setOrganisationName("latest-previous-org");
            expected.setToTimestamp(latestDate);
            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnNull_whenOrgPolicyIsNull() {
            var orgPolicy = new OrganisationPolicy();
            assertNull(OrgPolicyUtils.getLatestOrganisationChanges(orgPolicy));
        }

        @Test
        void shouldReturnNull_whenPreviousOrganisationsIsNull() {
            assertNull(OrgPolicyUtils.getLatestOrganisationChanges(null));
        }

        private PreviousOrganisationCollectionItem buildPreviousOrganisation(String name, LocalDateTime toDate) {
            PreviousOrganisation previousOrganisation = new PreviousOrganisation();
            previousOrganisation.setOrganisationName(name);
            previousOrganisation.setToTimestamp(toDate);

            return new PreviousOrganisationCollectionItem(null, previousOrganisation);
        }
    }

    @Nested
    class AddMissingOrgPolicies {
        @Test
        void shouldPopulateGivenCaseDataBuilderWithExpectedRespondentOrgPolicies_whenBothDoNotExist() {
            var caseDataBuilder = CaseData.builder();

            var caseData = caseDataBuilder.build();
            OrgPolicyUtils.addMissingOrgPolicies(caseData);

            // Use setters to update the builder's internal state to match the modified caseData
            caseDataBuilder.respondent1OrganisationPolicy(caseData.getRespondent1OrganisationPolicy());
            caseDataBuilder.respondent2OrganisationPolicy(caseData.getRespondent2OrganisationPolicy());
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
                    new OrganisationPolicy().setOrgPolicyCaseAssignedRole("[RES1-EXISTING]"));

            var caseData = caseDataBuilder.build();
            OrgPolicyUtils.addMissingOrgPolicies(caseData);

            // Use setters to update the builder's internal state to match the modified caseData
            caseDataBuilder.respondent1OrganisationPolicy(caseData.getRespondent1OrganisationPolicy());
            caseDataBuilder.respondent2OrganisationPolicy(caseData.getRespondent2OrganisationPolicy());
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
                    new OrganisationPolicy().setOrgPolicyCaseAssignedRole("[RES2-EXISTING]"));

            var caseData = caseDataBuilder.build();
            OrgPolicyUtils.addMissingOrgPolicies(caseData);

            // Use setters to update the builder's internal state to match the modified caseData
            caseDataBuilder.respondent1OrganisationPolicy(caseData.getRespondent1OrganisationPolicy());
            caseDataBuilder.respondent2OrganisationPolicy(caseData.getRespondent2OrganisationPolicy());
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
                        new OrganisationPolicy().setOrgPolicyCaseAssignedRole("[RES1-EXISTING]"))
                    .respondent2OrganisationPolicy(
                        new OrganisationPolicy().setOrgPolicyCaseAssignedRole("[RES2-EXISTING]"));

            var caseData = caseDataBuilder.build();
            OrgPolicyUtils.addMissingOrgPolicies(caseData);

            // Use setters to update the builder's internal state to match the modified caseData
            caseDataBuilder.respondent1OrganisationPolicy(caseData.getRespondent1OrganisationPolicy());
            caseDataBuilder.respondent2OrganisationPolicy(caseData.getRespondent2OrganisationPolicy());
            var actual = caseDataBuilder.build();

            assertEquals("[RES1-EXISTING]",
                         actual.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole());
            assertEquals("[RES2-EXISTING]",
                         actual.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }
    }
}
