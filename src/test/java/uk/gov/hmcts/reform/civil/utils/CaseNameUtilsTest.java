package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseNameInternal;

class CaseNameUtilsTest {

    @Nested
    class GetFormattedPartyName {

        @Test
        void shouldReturnExpectedPartyName_withValidParty() {
            var party = Party.builder()
                .individualFirstName("Applicant")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build();

            var actual = CaseNameUtils.getFormattedPartyName(party);

            assertEquals("'Applicant One'", actual);
        }

        @Test
        void shouldReturnEmptyString_withNullParty() {
            var actual = CaseNameUtils.getFormattedPartyName(null);

            assertEquals("", actual);
        }
    }

    @Nested
    class GetFormattedLitigationFriendName {

        @Test
        void shouldReturnExpectedLitigationFriendName_withValidLitigationFriend() {
            var litigationFriend = LitigationFriend.builder().firstName("Litigation").lastName("Friend").build();

            var actual = CaseNameUtils.getFormattedLitigationFriendName(litigationFriend);

            assertEquals(" represented by 'Litigation Friend' (litigation friend)", actual);
        }

        @Test
        void shouldReturnEmptyString_withNullLitigationFriend() {
            var actual = CaseNameUtils.getFormattedLitigationFriendName(null);

            assertEquals("", actual);
        }
    }

    @Nested
    class BuildCaseNamePublic {
        Party applicant1;
        Party applicant2;
        Party respondent1;
        Party respondent2;
        LitigationFriend applicant1LitigationFriend;
        LitigationFriend applicant2LitigationFriend;

        @BeforeEach
        void setupParties() {
            applicant1 = Party.builder()
                .individualFirstName("Applicant")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build();

            applicant1LitigationFriend = LitigationFriend.builder()
                .firstName("ApplicantOne")
                .lastName("LitigationFriend")
                .build();

            applicant2 = Party.builder()
                .individualFirstName("Applicant")
                .individualLastName("Two")
                .type(Party.Type.INDIVIDUAL)
                .build();

            applicant2LitigationFriend = LitigationFriend.builder()
                .firstName("ApplicantTwo")
                .lastName("LitigationFriend")
                .build();

            respondent1 = Party.builder()
                .individualFirstName("Respondent")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL)
                .build();

            respondent2 = Party.builder()
                .individualFirstName("Respondent")
                .individualLastName("Two")
                .type(Party.Type.INDIVIDUAL)
                .build();
        }

        @Test
        void shouldReturnExpectedCaseName_with1v1PartyData() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .build();

            var actual = CaseNameUtils.buildCaseNamePublic(caseData);

            assertEquals("'Applicant One' v 'Respondent One'", actual);
        }

        @Test
        void shouldReturnExpectedCaseName_with1v1PartyData_app1LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .respondent1(respondent1)
                .build();

            var actual = CaseNameUtils.buildCaseNamePublic(caseData);

            assertEquals("'Applicant One' represented by 'ApplicantOne LitigationFriend' (litigation friend)" +
                             " v 'Respondent One'", actual);
        }

        @Test
        void shouldReturnExpectedCaseName_with1v2PartyData() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .build();

            var actual = CaseNameUtils.buildCaseNamePublic(caseData);

            assertEquals("'Applicant One' v 'Respondent One', 'Respondent Two'", actual);
        }

        @Test
        void shouldReturnExpectedCaseName_with1v2PartyData_app1LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .build();

            var actual = CaseNameUtils.buildCaseNamePublic(caseData);

            assertEquals("'Applicant One' represented by 'ApplicantOne LitigationFriend'" +
                             " (litigation friend) v 'Respondent One', 'Respondent Two'", actual);
        }

        @Test
        void shouldReturnExpectedCaseName_with2v1PartyData() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .build();

            var actual = CaseNameUtils.buildCaseNamePublic(caseData);

            assertEquals("'Applicant One', 'Applicant Two' v 'Respondent One'", actual);
        }

        @Test
        void shouldReturnExpectedCaseName_with2v1PartyData_app1LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .build();

            var actual = CaseNameUtils.buildCaseNamePublic(caseData);

            assertEquals("'Applicant One' represented by 'ApplicantOne LitigationFriend' (litigation friend), " +
                             "'Applicant Two' v 'Respondent One'", actual);
        }

        @Test
        void shouldReturnExpectedCaseName_with2v1PartyData_app2LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .applicant2LitigationFriend(applicant2LitigationFriend)
                .respondent1(respondent1)
                .build();

            var actual = CaseNameUtils.buildCaseNamePublic(caseData);

            assertEquals("'Applicant One', " +
                             "'Applicant Two' represented by 'ApplicantTwo LitigationFriend' (litigation friend)" +
                             " v 'Respondent One'", actual);
        }

        @Test
        void shouldReturnExpectedCaseName_with2v1PartyData_bothAppsLiFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .applicant2(applicant2)
                .applicant2LitigationFriend(applicant2LitigationFriend)
                .respondent1(respondent1)
                .build();

            var actual = CaseNameUtils.buildCaseNamePublic(caseData);

            assertEquals("'Applicant One' represented by 'ApplicantOne LitigationFriend' (litigation friend), " +
                             "'Applicant Two' represented by 'ApplicantTwo LitigationFriend' (litigation friend) " +
                             "v 'Respondent One'", actual);
        }
    }

    @Nested
    class BuildCaseNameInternal {

        @Test
        void shouldReturnCaseName_when1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();

            String actual = buildCaseNameInternal(caseData);

            assertThat(actual).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        }

        @Test
        void shouldReturnCaseName_when1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            String actual = buildCaseNameInternal(caseData);

            assertThat(actual).isEqualTo("Mr. John Rambo v Mr. Sole Trader and Mr. John Rambo");
        }

        @Test
        void shouldReturnCaseName_when2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .build();

            String actual = buildCaseNameInternal(caseData);

            assertThat(actual).isEqualTo("Mr. John Rambo and Mr. Jason Rambo v Mr. Sole Trader");
        }
    }
}
