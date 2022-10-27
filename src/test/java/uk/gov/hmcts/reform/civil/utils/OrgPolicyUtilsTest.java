package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisation;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisationCollectionItem;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils.getLatestNoCEvent;

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
    class GetLatestNoCEvent {

        @Test
        void shouldReturnNull_whenThereAreNoOrgPolicies() {
            var caseData = CaseDataBuilder.builder().build();
            assertEquals(null, getLatestNoCEvent(caseData));
        }

        @Test
        void shouldReturnNull_whenThereAreNoPreviousOrganisations() {
            var caseData = CaseDataBuilder.builder()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
                .build();

            assertEquals(null, getLatestNoCEvent(caseData));
        }

        @Test
        void shouldReturnLatestOrganisationChange_whenApplicant1HasChanged() {
            var previousOrg = buildPreviousOrg(
                "organisation",
                LocalDateTime.of(2021, 01, 01, 00, 00)
            );

            var caseData = CaseDataBuilder.builder()
                .applicant1OrganisationPolicy(
                    OrganisationPolicy.builder().previousOrganisations(List.of(previousOrg)).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
                .build();

            assertEquals(previousOrg.getValue(), getLatestNoCEvent(caseData));
        }

        @Test
        void shouldReturnLatestOrganisationChange_whenRespondent1HasChanged() {
            var previousOrg = buildPreviousOrg(
                "organisation",
                LocalDateTime.of(2021, 01, 01, 00, 00)
            );

            var caseData = CaseDataBuilder.builder()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent1OrganisationPolicy(
                    OrganisationPolicy.builder().previousOrganisations(List.of(previousOrg)).build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
                .build();

            assertEquals(previousOrg.getValue(), getLatestNoCEvent(caseData));
        }

        @Test
        void shouldReturnLatestOrganisationChange_whenRespondent2HasChanged() {
            var previousOrg = buildPreviousOrg(
                "organisation",
                LocalDateTime.of(2021, 01, 01, 00, 00)
            );

            var caseData = CaseDataBuilder.builder()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .previousOrganisations(List.of(previousOrg)).build())
                .build();

            assertEquals(previousOrg.getValue(), getLatestNoCEvent(caseData));
        }

        @Test
        void shouldReturnLatestOrganisationChange_whenMultipleChangesHaveHappened() {
            var previousOrg = buildPreviousOrg(
                "organisation 1",
                LocalDateTime.of(2021, 01, 01, 00, 00)
            );

            var latestPreviousOrg = buildPreviousOrg(
                "organisation 2",
                LocalDateTime.of(2021, 03, 01, 00, 00)
            );

            var caseData = CaseDataBuilder.builder()
                .applicant1OrganisationPolicy((OrganisationPolicy.builder()
                    .previousOrganisations(List.of(latestPreviousOrg)).build()))
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .previousOrganisations(List.of(previousOrg)).build())
                .build();

            assertEquals(latestPreviousOrg.getValue(), getLatestNoCEvent(caseData));
        }

        PreviousOrganisationCollectionItem buildPreviousOrg(String name, LocalDateTime from) {
            var prevOrg = PreviousOrganisation.builder()
                .organisationName(name)
                .fromTimestamp(from)
                .toTimestamp(from.plusYears(1))
                .build();
            return PreviousOrganisationCollectionItem
                .builder()
                .value(prevOrg)
                .build();
        }
    }
}
