package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Flags;
import uk.gov.hmcts.reform.civil.model.DQPartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.FlagsUtils.APPLICANT_SOLICITOR_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.FlagsUtils.APPLICANT_SOLICITOR_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.FlagsUtils.RESPONDENT_SOLICITOR_ONE_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.FlagsUtils.RESPONDENT_SOLICITOR_ONE_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.FlagsUtils.RESPONDENT_SOLICITOR_TWO_EXPERT;
import static uk.gov.hmcts.reform.civil.utils.FlagsUtils.RESPONDENT_SOLICITOR_TWO_WITNESS;
import static uk.gov.hmcts.reform.civil.utils.FlagsUtils.addApplicantExpertAndWitnessFlagsStructure;
import static uk.gov.hmcts.reform.civil.utils.FlagsUtils.addRespondentDQPartiesFlagStructure;

public class FlagsUtilsTest {

    @Test
    public void shouldCreateFlagsStructureForRespondentExperts() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .multiPartyClaimTwoDefendantSolicitors()
            .build();

        Expert expert1 = Expert.builder().firstName("First").lastName("Name").build();
        Expert expert2 = Expert.builder().firstName("Second").lastName("expert").build();
        Expert expert3 = Expert.builder().firstName("Third").lastName("experto").build();

        CaseData updatedCaseData = caseData.toBuilder()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts
                                                         .builder()
                                                         .details(wrapElements(expert1, expert2))
                                                         .build())
                               .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQExperts(Experts.builder()
                                                         .details(wrapElements(expert3))
                                                         .build())
                               .build())
            .build();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilderToUpdateWithFlags = updatedCaseData.toBuilder();

        addRespondentDQPartiesFlagStructure(
            caseDataBuilderToUpdateWithFlags,
            updatedCaseData);

        CaseData caseDataWithFlags = caseDataBuilderToUpdateWithFlags.build();
        List<Element<DQPartyFlagStructure>> respondentSolicitor1ExpertsWithFlags = caseDataWithFlags.getRespondentSolicitor1Experts();
        List<Element<DQPartyFlagStructure>> respondentSolicitor2ExpertsWithFlags = caseDataWithFlags.getRespondentSolicitor2Experts();

        Flags expectedExpert1Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
            .partyName("First Name")
            .details(List.of()).build();

        Flags expectedExpert2Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_ONE_EXPERT)
            .partyName("Second expert")
            .details(List.of()).build();

        Flags expectedExpert3Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_TWO_EXPERT)
            .partyName("Third experto")
            .details(List.of()).build();

        assertThat(respondentSolicitor1ExpertsWithFlags).isNotNull();
        assertThat(respondentSolicitor1ExpertsWithFlags).hasSize(2);

        assertThat(respondentSolicitor1ExpertsWithFlags.get(0).getValue().getFlags()).isEqualTo(expectedExpert1Flags);
        assertThat(respondentSolicitor1ExpertsWithFlags.get(0).getValue().getFirstName()).isEqualTo("First");
        assertThat(respondentSolicitor1ExpertsWithFlags.get(0).getValue().getLastName()).isEqualTo("Name");

        assertThat(respondentSolicitor1ExpertsWithFlags.get(1).getValue().getFlags()).isEqualTo(expectedExpert2Flags);
        assertThat(respondentSolicitor1ExpertsWithFlags.get(1).getValue().getFirstName()).isEqualTo("Second");
        assertThat(respondentSolicitor1ExpertsWithFlags.get(1).getValue().getLastName()).isEqualTo("expert");

        assertThat(respondentSolicitor2ExpertsWithFlags).isNotNull();
        assertThat(respondentSolicitor2ExpertsWithFlags).hasSize(1);

        assertThat(respondentSolicitor2ExpertsWithFlags.get(0).getValue().getFlags()).isEqualTo(expectedExpert3Flags);
        assertThat(respondentSolicitor2ExpertsWithFlags.get(0).getValue().getFirstName()).isEqualTo("Third");
        assertThat(respondentSolicitor2ExpertsWithFlags.get(0).getValue().getLastName()).isEqualTo("experto");
    }

    @Test
    public void shouldCreateFlagsStructureForRespondentWitness() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .multiPartyClaimTwoDefendantSolicitors()
            .build();

        Witness witness1 = Witness.builder().firstName("First").lastName("Name").build();
        Witness witness2 = Witness.builder().firstName("Second").lastName("witness").build();
        Witness witness3 = Witness.builder().firstName("Third").lastName("witnessy").build();

        CaseData updatedCaseData = caseData.toBuilder()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQWitnesses(Witnesses
                                                         .builder()
                                                         .details(wrapElements(witness1, witness2))
                                                         .build())
                               .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQWitnesses(Witnesses
                                                         .builder()
                                                         .details(wrapElements(witness3))
                                                         .build())
                               .build())
            .build();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilderToUpdateWithFlags = updatedCaseData.toBuilder();

        addRespondentDQPartiesFlagStructure(
            caseDataBuilderToUpdateWithFlags,
            updatedCaseData);

        CaseData caseDataWithFlags = caseDataBuilderToUpdateWithFlags.build();
        List<Element<DQPartyFlagStructure>> respondentSolicitor1WitnessWithFlags = caseDataWithFlags.getRespondentSolicitor1Witnesses();
        List<Element<DQPartyFlagStructure>> respondentSolicitor2WitnessWithFlags = caseDataWithFlags.getRespondentSolicitor2Witnesses();

        Flags expectedWitness1Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
            .partyName("First Name")
            .details(List.of()).build();

        Flags expectedWitness2Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_ONE_WITNESS)
            .partyName("Second witness")
            .details(List.of()).build();

        Flags expectedWitness3Flags = Flags.builder().roleOnCase(RESPONDENT_SOLICITOR_TWO_WITNESS)
            .partyName("Third witnessy")
            .details(List.of()).build();

        assertThat(respondentSolicitor1WitnessWithFlags).isNotNull();
        assertThat(respondentSolicitor1WitnessWithFlags).hasSize(2);

        assertThat(respondentSolicitor1WitnessWithFlags.get(0).getValue().getFlags()).isEqualTo(expectedWitness1Flags);
        assertThat(respondentSolicitor1WitnessWithFlags.get(0).getValue().getFirstName()).isEqualTo("First");
        assertThat(respondentSolicitor1WitnessWithFlags.get(0).getValue().getLastName()).isEqualTo("Name");

        assertThat(respondentSolicitor1WitnessWithFlags.get(1).getValue().getFlags()).isEqualTo(expectedWitness2Flags);
        assertThat(respondentSolicitor1WitnessWithFlags.get(1).getValue().getFirstName()).isEqualTo("Second");
        assertThat(respondentSolicitor1WitnessWithFlags.get(1).getValue().getLastName()).isEqualTo("witness");

        assertThat(respondentSolicitor2WitnessWithFlags).isNotNull();
        assertThat(respondentSolicitor2WitnessWithFlags).hasSize(1);

        assertThat(respondentSolicitor2WitnessWithFlags.get(0).getValue().getFlags()).isEqualTo(expectedWitness3Flags);
        assertThat(respondentSolicitor2WitnessWithFlags.get(0).getValue().getFirstName()).isEqualTo("Third");
        assertThat(respondentSolicitor2WitnessWithFlags.get(0).getValue().getLastName()).isEqualTo("witnessy");
    }

    @Test
    public void shouldCreateFlagsStructureForApplicantWitness() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
            .multiPartyClaimTwoApplicants()
            .build();

        Witness witness1 = Witness.builder().firstName("First").lastName("Name").build();
        Witness witness2 = Witness.builder().firstName("Second").lastName("witness").build();
        Witness witness3 = Witness.builder().firstName("Third").lastName("witnessy").build();

        CaseData updatedCaseData = caseData.toBuilder()
            .applicant1DQ(Applicant1DQ.builder()
                               .applicant1DQWitnesses(Witnesses
                                                           .builder()
                                                           .details(wrapElements(witness1, witness2))
                                                           .build())
                               .build())
            .applicant2DQ(Applicant2DQ.builder()
                               .applicant2DQWitnesses(Witnesses
                                                           .builder()
                                                           .details(wrapElements(witness3))
                                                           .build())
                               .build())
            .build();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilderToUpdateWithFlags = updatedCaseData.toBuilder();

        addApplicantExpertAndWitnessFlagsStructure(
            caseDataBuilderToUpdateWithFlags,
            updatedCaseData);

        CaseData caseDataWithFlags = caseDataBuilderToUpdateWithFlags.build();
        List<Element<DQPartyFlagStructure>> applicantSolicitorWitnesses = caseDataWithFlags.getApplicantSolicitorWitnesses();

        Flags expectedWitness1Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_WITNESS)
            .partyName("First Name")
            .details(List.of()).build();

        Flags expectedWitness2Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_WITNESS)
            .partyName("Second witness")
            .details(List.of()).build();

        Flags expectedWitness3Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_WITNESS)
            .partyName("Third witnessy")
            .details(List.of()).build();

        assertThat(applicantSolicitorWitnesses).isNotNull();
        assertThat(applicantSolicitorWitnesses).hasSize(3);

        assertThat(applicantSolicitorWitnesses.get(0).getValue().getFlags()).isEqualTo(expectedWitness1Flags);
        assertThat(applicantSolicitorWitnesses.get(0).getValue().getFirstName()).isEqualTo("First");
        assertThat(applicantSolicitorWitnesses.get(0).getValue().getLastName()).isEqualTo("Name");

        assertThat(applicantSolicitorWitnesses.get(1).getValue().getFlags()).isEqualTo(expectedWitness2Flags);
        assertThat(applicantSolicitorWitnesses.get(1).getValue().getFirstName()).isEqualTo("Second");
        assertThat(applicantSolicitorWitnesses.get(1).getValue().getLastName()).isEqualTo("witness");

        assertThat(applicantSolicitorWitnesses.get(2).getValue().getFlags()).isEqualTo(expectedWitness3Flags);
        assertThat(applicantSolicitorWitnesses.get(2).getValue().getFirstName()).isEqualTo("Third");
        assertThat(applicantSolicitorWitnesses.get(2).getValue().getLastName()).isEqualTo("witnessy");
    }

    @Test
    public void shouldCreateFlagsStructureForApplicantExperts() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
            .multiPartyClaimTwoApplicants()
            .build();

        Expert expert1 = Expert.builder().firstName("First").lastName("Name").build();
        Expert expert2 = Expert.builder().firstName("Second").lastName("expert").build();
        Expert expert3 = Expert.builder().firstName("Third").lastName("experto").build();

        CaseData updatedCaseData = caseData.toBuilder()
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQExperts(Experts
                                                         .builder()
                                                         .details(wrapElements(expert1, expert2))
                                                         .build())
                              .build())
            .applicant2DQ(Applicant2DQ.builder()
                              .applicant2DQExperts(Experts
                                                         .builder()
                                                         .details(wrapElements(expert3))
                                                         .build())
                              .build())
            .build();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilderToUpdateWithFlags = updatedCaseData.toBuilder();

        addApplicantExpertAndWitnessFlagsStructure(
            caseDataBuilderToUpdateWithFlags,
            updatedCaseData);

        CaseData caseDataWithFlags = caseDataBuilderToUpdateWithFlags.build();
        List<Element<DQPartyFlagStructure>> applicantSolicitorExperts = caseDataWithFlags.getApplicantSolicitorExperts();

        Flags expectedExpert1Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_EXPERT)
            .partyName("First Name")
            .details(List.of()).build();

        Flags expectedExpert2Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_EXPERT)
            .partyName("Second expert")
            .details(List.of()).build();

        Flags expectedExpert3Flags = Flags.builder().roleOnCase(APPLICANT_SOLICITOR_EXPERT)
            .partyName("Third experto")
            .details(List.of()).build();

        assertThat(applicantSolicitorExperts).isNotNull();
        assertThat(applicantSolicitorExperts).hasSize(3);

        assertThat(applicantSolicitorExperts.get(0).getValue().getFlags()).isEqualTo(expectedExpert1Flags);
        assertThat(applicantSolicitorExperts.get(0).getValue().getFirstName()).isEqualTo("First");
        assertThat(applicantSolicitorExperts.get(0).getValue().getLastName()).isEqualTo("Name");

        assertThat(applicantSolicitorExperts.get(1).getValue().getFlags()).isEqualTo(expectedExpert2Flags);
        assertThat(applicantSolicitorExperts.get(1).getValue().getFirstName()).isEqualTo("Second");
        assertThat(applicantSolicitorExperts.get(1).getValue().getLastName()).isEqualTo("expert");

        assertThat(applicantSolicitorExperts.get(2).getValue().getFlags()).isEqualTo(expectedExpert3Flags);
        assertThat(applicantSolicitorExperts.get(2).getValue().getFirstName()).isEqualTo("Third");
        assertThat(applicantSolicitorExperts.get(2).getValue().getLastName()).isEqualTo("experto");
    }
}
