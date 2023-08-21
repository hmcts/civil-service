package uk.gov.hmcts.reform.civil.utils;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicantOptions2v1;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant2Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendantOptions1v2SameSolicitor;

class ManageContactInformationUtilsTest {

    @Test
    void shouldAddCorrectOptions_forClaimant1AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .addApplicant1ExpertsAndWitnesses().build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addApplicant1Options(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedApplicant1Options(true, false));

        List<String> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addApplicant1Options(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedApplicant1Options(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forClaimants2v1AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .multiPartyClaimTwoApplicants()
            .applicant2DQ()
            .addApplicant2ExpertsAndWitnesses()
            .addApplicant1ExpertsAndWitnesses().build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addApplicantOptions2v1(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedApplicants2v1Options(true, false));

        List<String> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addApplicantOptions2v1(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedApplicants2v1Options(false, false));
    }


    @Test
    void shouldAddCorrectOptions_forClaimant1AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addApplicant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicant1Options(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forClaimants2v1AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .addApplicant2LitigationFriend()
            .multiPartyClaimTwoApplicants()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addApplicantOptions2v1(options, caseData, true);

        assertThat(options).isEqualTo(expectedApplicants2v1Options(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant1AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .addRespondent1ExpertsAndWitnesses()
            .build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addDefendant1Options(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedDefendant1Options(true, false));

        List<String> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addDefendant1Options(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedDefendant1Options(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant1AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addDefendant1Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedDefendant1Options(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant2AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_TWO_TWO_LEGAL_REP)
            .addRespondent2LitigationFriend()
            .addRespondent2ExpertsAndWitnesses()
            .build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .addRespondent2LitigationFriend()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2Represented(YES)
            .addRespondent2LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addDefendant2Options(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedDefendant2Options(true, false));

        List<String> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addDefendant2Options(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedDefendant2Options(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant2AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent2(YES)
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2Represented(YES)
            .addRespondent2LitigationFriend()
            .respondent2Responds(FULL_DEFENCE).build();

        List<String> options = new ArrayList<>();
        addDefendant2Options(options, caseData, true);

        assertThat(options).isEqualTo(expectedDefendant2Options(true, true));
    }

    @Test
    void shouldAddCorrectOptions_forDefendants1v2SameSolicitorAsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefence_1v2_Resp1CounterClaimAndResp2FullDefence()
            .addRespondent1LitigationFriend()
            .addRespondent2LitigationFriend()
            .addRespondent2ExpertsAndWitnesses()
            .addRespondent1ExpertsAndWitnesses()
            .build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .addRespondent1LitigationFriend()
            .addRespondent2LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addDefendantOptions1v2SameSolicitor(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedDefendants1v2SameSolicitorOptions(true, false));

        List<String> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addDefendantOptions1v2SameSolicitor(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedDefendants1v2SameSolicitorOptions(false, false));
    }

    @Test
    void shouldAddCorrectOptions_forDefendant1v2SameSolicitorAsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent2(YES)
            .multiPartyClaimOneDefendantSolicitor()
            .addRespondent2LitigationFriend()
            .addRespondent1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addDefendantOptions1v2SameSolicitor(options, caseData, true);

        assertThat(options).isEqualTo(expectedDefendants1v2SameSolicitorOptions(true, true));
    }

    private List<String> expectedApplicant1Options(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<String> list = new ArrayList<>();
        list.add("CLAIMANT 1: Mr. John Rambo");
        list.add("CLAIMANT 1: Litigation Friend: Applicant Litigation Friend");
        list.add("CLAIMANT 1: Individuals attending for the legal representative");
        if (withExpertsAndWitnesses || isAdmin) {
            list.add("CLAIMANT 1: Witnesses");
            list.add("CLAIMANT 1: Experts");
        }
        return list;
    }

    private List<String> expectedApplicants2v1Options(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<String> list = new ArrayList<>();
        list.add("CLAIMANT 1: Mr. John Rambo");
        list.add("CLAIMANT 1: Litigation Friend: Applicant Litigation Friend");
        list.add("CLAIMANT 2: Mr. Jason Rambo");
        list.add("CLAIMANT 2: Litigation Friend: Applicant Two Litigation Friend");
        list.add("CLAIMANTS: Individuals attending for the legal representative");
        if (withExpertsAndWitnesses || isAdmin) {
            list.add("CLAIMANTS: Witnesses");
            list.add("CLAIMANTS: Experts");
        }
        return list;
    }

    private List<String> expectedDefendant1Options(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<String> list = new ArrayList<>();
        list.add("DEFENDANT 1: Mr. Sole Trader");
        list.add("DEFENDANT 1: Litigation Friend: Litigation Friend");
        list.add("DEFENDANT 1: Individuals attending for the legal representative");
        if (withExpertsAndWitnesses || isAdmin) {
            list.add("DEFENDANT 1: Witnesses");
            list.add("DEFENDANT 1: Experts");
        }
        return list;
    }

    private List<String> expectedDefendant2Options(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<String> list = new ArrayList<>();
        list.add("DEFENDANT 2: Mr. John Rambo");
        list.add("DEFENDANT 2: Litigation Friend: Litigation Friend");
        list.add("DEFENDANT 2: Individuals attending for the legal representative");
        if (withExpertsAndWitnesses || isAdmin) {
            list.add("DEFENDANT 2: Witnesses");
            list.add("DEFENDANT 2: Experts");
        }
        return list;
    }

    private List<String> expectedDefendants1v2SameSolicitorOptions(boolean withExpertsAndWitnesses, boolean isAdmin) {
        List<String> list = new ArrayList<>();
        list.add("DEFENDANT 1: Mr. Sole Trader");
        list.add("DEFENDANT 1: Litigation Friend: Litigation Friend");
        list.add("DEFENDANT 2: Mr. John Rambo");
        list.add("DEFENDANT 2: Litigation Friend: Litigation Friend");
        list.add("DEFENDANTS: Individuals attending for the legal representative");
        if (withExpertsAndWitnesses || isAdmin) {
            list.add("DEFENDANTS: Witnesses");
            list.add("DEFENDANTS: Experts");
        }
        return list;
    }
}
