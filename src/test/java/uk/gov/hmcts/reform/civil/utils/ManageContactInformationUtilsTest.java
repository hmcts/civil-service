package uk.gov.hmcts.reform.civil.utils;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicant1Options;

class ManageContactInformationUtilsTest {

    @Test
    void shouldAddCorrectOptions_forClaimant1v1AsLegalRep() {
        CaseData caseDataWithExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed()
            .addApplicant1ExpertsAndWitnesses().build();

        CaseData caseDataWithoutExpertsAndWitnesses = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addApplicant1Options(options, caseDataWithExpertsAndWitnesses, false);
        assertThat(options).isEqualTo(expectedApplicantOptions1v1(true, false));

        List<String> optionsWithoutExpertsAndWitnesses = new ArrayList<>();
        addApplicant1Options(optionsWithoutExpertsAndWitnesses, caseDataWithoutExpertsAndWitnesses, false);
        assertThat(optionsWithoutExpertsAndWitnesses).isEqualTo(expectedApplicantOptions1v1(false, false));

    }

    @Test
    void shouldAddCorrectOptions_forClaimant1v1AsAdmin() {
        CaseData caseData = CaseDataBuilder.builder()
            .addApplicant1LitigationFriend()
            .atStateApplicantRespondToDefenceAndProceed().build();

        List<String> options = new ArrayList<>();
        addApplicant1Options(options, caseData, false);

        List<String> expectedOptionsWithAllParties = expectedApplicantOptions1v1(true, true);

        assertThat(options).isEqualTo(expectedOptionsWithAllParties);
    }

    private List<String> expectedApplicantOptions1v1(boolean withExpertsAndWitnesses, boolean isAdmin) {
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
}
