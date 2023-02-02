package uk.gov.hmcts.reform.civil.service.robotics.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisation;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisationCollectionItem;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoboticsDataUtilsTest {

    @Nested
    class BuildNoticeOfChange {
        @Test
        void shouldReturnNull_whenNoPreviousOrganisationsExist() {
            var caseData = CaseDataBuilder.builder()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
                .build();

            assertNull(RoboticsDataUtil.buildNoticeOfChange(caseData));
        }

        @Test
        void shouldReturnLatestOrganisationChanges_whenChangesExistForAll() {
            var latestDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
            var oldestDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");

            var caseData = CaseDataBuilder.builder()
                .applicant1OrganisationPolicy(
                    OrganisationPolicy.builder().previousOrganisations(List.of(
                        buildPreviousOrganisation("app-1-latest", latestDate),
                        buildPreviousOrganisation("app-1-old", oldestDate)
                    )).build()
                )
                .respondent1OrganisationPolicy(
                    OrganisationPolicy.builder().previousOrganisations(List.of(
                        buildPreviousOrganisation("res-1-latest", latestDate),
                        buildPreviousOrganisation("res-1-old", oldestDate)
                    )).build())
                .respondent2OrganisationPolicy(
                    OrganisationPolicy.builder().previousOrganisations(List.of(
                        buildPreviousOrganisation("res-2-old", oldestDate),
                        buildPreviousOrganisation("res-2-latest", latestDate)
                    )).build())
                .build();

            var actual = RoboticsDataUtil.buildNoticeOfChange(caseData);

            assertEquals(3, actual.size());
            assertEquals("001", actual.get(0).getLitigiousPartyID());
            assertEquals("2022-02-01", actual.get(0).getDateOfNoC());

            assertEquals("002", actual.get(1).getLitigiousPartyID());
            assertEquals("2022-02-01", actual.get(1).getDateOfNoC());

            assertEquals("003", actual.get(2).getLitigiousPartyID());
            assertEquals("2022-02-01", actual.get(2).getDateOfNoC());
        }

        @Test
        void shouldReturnLatestOrganisationChanges_whenChangesExistForSome() {
            var latestDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
            var oldestDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");

            var caseData = CaseDataBuilder.builder()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent1OrganisationPolicy(
                    OrganisationPolicy.builder().previousOrganisations(List.of(
                        buildPreviousOrganisation("res-1-latest", latestDate),
                        buildPreviousOrganisation("res-1-old", oldestDate)
                    )).build())
                .respondent2OrganisationPolicy(
                    OrganisationPolicy.builder().previousOrganisations(List.of(
                        buildPreviousOrganisation("res-2-old", oldestDate),
                        buildPreviousOrganisation("res-2-latest", latestDate)
                    )).build())
                .build();

            var actual = RoboticsDataUtil.buildNoticeOfChange(caseData);

            assertEquals(2, actual.size());
            assertEquals("002", actual.get(0).getLitigiousPartyID());
            assertEquals("2022-02-01", actual.get(0).getDateOfNoC());

            assertEquals("003", actual.get(1).getLitigiousPartyID());
            assertEquals("2022-02-01", actual.get(1).getDateOfNoC());
        }

        private PreviousOrganisationCollectionItem buildPreviousOrganisation(String name, LocalDateTime toDate) {
            return PreviousOrganisationCollectionItem.builder().value(
                PreviousOrganisation.builder().organisationName(name).toTimestamp(toDate).build()).build();
        }
    }

}
