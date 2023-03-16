package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceHearingsCaseLevelMapperTest {

    @Nested
    class GetPublicCaseName {

        Party applicant1;
        Party respondent1;

        @BeforeEach
        void setupParties() {
            applicant1 = Party.builder()
                .individualFirstName("Applicant")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build();

            respondent1 = Party.builder()
                .individualFirstName("Respondent")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build();
        }

        @Test
        void shouldReturnExpectedPublicCaseName_whenCaseNamePublicExists() {
            var expected = "'A Somebody' vs 'Somebody else'";
            var caseData = CaseData.builder()
                .caseNamePublic(expected)
                .applicant1(applicant1)
                .respondent1(respondent1)
                .build();

            var actual = ServiceHearingsCaseLevelMapper.getPublicCaseName(caseData);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedPublicCaseName_whenCaseNamePublicDoesNotExist() {
            var expected = "'Applicant One' v 'Respondent One'";
            var caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .build();

            var actual = ServiceHearingsCaseLevelMapper.getPublicCaseName(caseData);

            assertThat(actual).isEqualTo(expected);
        }
    }
}
