package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
class PersistDataUtilsTest {

    @Test
    void shouldCopyFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .build();

        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .withApplicant1Flags()
            .withApplicant2Flags()
            .withRespondent1Flags()
            .withRespondent2Flags().build();

        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        PersistDataUtils.persistFlagsForParties(oldCaseData, caseData, builder);
        CaseData results = builder.build();
        assertThat(results.getApplicant1().getFlags()).isNotNull();
        assertThat(results.getApplicant1().getFlags().getRoleOnCase()).isEqualTo("Claimant 1");
        assertThat(results.getApplicant2().getFlags()).isNotNull();
        assertThat(results.getApplicant2().getFlags().getRoleOnCase()).isEqualTo("Claimant 2");
        assertThat(results.getRespondent1().getFlags()).isNotNull();
        assertThat(results.getRespondent1().getFlags().getRoleOnCase()).isEqualTo("Defendant 1");
        assertThat(results.getRespondent2().getFlags()).isNotNull();
        assertThat(results.getRespondent2().getFlags().getRoleOnCase()).isEqualTo("Defendant 2");
    }

    @Test
    void shouldCopyFlags_1V1() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .build();

        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .withApplicant1Flags()
            .withRespondent1Flags()
            .build();

        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        PersistDataUtils.persistFlagsForParties(oldCaseData, caseData, builder);
        CaseData results = builder.build();
        assertThat(results.getApplicant1().getFlags()).isNotNull();
        assertThat(results.getApplicant1().getFlags().getRoleOnCase()).isEqualTo("Claimant 1");
        assertThat(results.getRespondent1().getFlags()).isNotNull();
        assertThat(results.getRespondent1().getFlags().getRoleOnCase()).isEqualTo("Defendant 1");
    }

    @Test
    void shouldCopyLitigationFriendFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .addRespondent1LitigationFriend()
            .addRespondent2LitigationFriend().build();

        CaseData oldCaseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .addRespondent1LitigationFriend()
            .addRespondent2LitigationFriend()
            .withApplicant1LitigationFriendFlags()
            .withApplicant2LitigationFriendFlags()
            .withRespondent1LitigationFriendFlags()
            .withRespondent2LitigationFriendFlags().build();

        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        PersistDataUtils.persistFlagsForLitigationFriendParties(oldCaseData, caseData, builder);
        CaseData results = builder.build();
        assertThat(results.getApplicant1LitigationFriend().getFlags()).isNotNull();
        assertThat(results.getApplicant1LitigationFriend().getFlags().getRoleOnCase()).isEqualTo("Claimant 1 Litigation Friend");
        assertThat(results.getApplicant2LitigationFriend().getFlags()).isNotNull();
        assertThat(results.getApplicant2LitigationFriend().getFlags().getRoleOnCase()).isEqualTo("Claimant 2 Litigation Friend");
        assertThat(results.getRespondent1LitigationFriend().getFlags()).isNotNull();
        assertThat(results.getRespondent1LitigationFriend().getFlags().getRoleOnCase()).isEqualTo("Defendant 1 Litigation Friend");
        assertThat(results.getRespondent2LitigationFriend().getFlags()).isNotNull();
        assertThat(results.getRespondent2LitigationFriend().getFlags().getRoleOnCase()).isEqualTo("Defendant 2 Litigation Friend");
    }

    @Test
    void shouldCopyAddress() {
        Address expectedAddress = Address.builder()
            .postCode("E11 5BB")
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).build())
            .build();

        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(expectedAddress).build())
            .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(expectedAddress).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(expectedAddress).build())
            .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(expectedAddress).build())
            .build();

        CaseData results = PersistDataUtils.persistPartyAddress(oldCaseData, caseData);
        assertThat(results.getApplicant1().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getApplicant2().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getRespondent1().getPrimaryAddress()).isEqualTo(expectedAddress);
        assertThat(results.getRespondent2().getPrimaryAddress()).isEqualTo(expectedAddress);
    }
}
