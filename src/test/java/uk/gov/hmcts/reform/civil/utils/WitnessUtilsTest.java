package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.EventAddedEvents;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToApplicantWitnesses;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

class WitnessUtilsTest {

    @Test
    void shouldNotAddEventAndDateAddedToRespondentWitnesses_1v1WhenNoWitnessesExist() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses respondent1DQWitnesses = updatedCaseData.getRespondent1DQ().getRespondent1DQWitnesses();

        assertThat(respondent1DQWitnesses.getDetails()).isNull();
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentWitnesses_1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .addRespondent1ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses respondent1DQWitnesses = updatedCaseData.getRespondent1DQ().getRespondent1DQWitnesses();

        for (Witness witness : unwrapElements(respondent1DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentWitnesses_1v2SSSingleResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
            .addRespondent1ExpertsAndWitnesses()
            .respondentResponseIsSame(YES)
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses respondent1DQWitnesses = updatedCaseData.getRespondent1DQ().getRespondent1DQWitnesses();
        Witnesses respondent2DQWitnesses = updatedCaseData.getRespondent2DQ().getRespondent2DQWitnesses();

        for (Witness witness : unwrapElements(respondent1DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }

        for (Witness witness : unwrapElements(respondent2DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentWitnesses_1v2SSDivergentResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateDivergentResponseWithRespondent2FullDefence1v2SameSol_NotSingleDQ()
            .addRespondent2ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses respondent2DQWitnesses = updatedCaseData.getRespondent2DQ().getRespondent2DQWitnesses();

        assertThat(updatedCaseData.getRespondent1DQ()).isNull();

        for (Witness witness : unwrapElements(respondent2DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getRespondent2ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentWitnesses_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateRespondentFullDefence()
            .respondent2Responds(RespondentResponseType.FULL_DEFENCE)
            .respondent2DQ()
            .addRespondent1ExpertsAndWitnesses()
            .addRespondent2ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses respondent1DQWitnesses = updatedCaseData.getRespondent1DQ().getRespondent1DQWitnesses();
        Witnesses respondent2DQWitnesses = updatedCaseData.getRespondent2DQ().getRespondent2DQWitnesses();

        for (Witness witness : unwrapElements(respondent1DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }

        for (Witness witness : unwrapElements(respondent2DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getRespondent2ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }
    }

    @Test
    void addEventAndDateAddedToApplicantWitnesses_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .addApplicant1ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToApplicantWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses applicant1DQWitnesses = updatedCaseData.getApplicant1DQ().getApplicant1DQWitnesses();

        for (Witness witness : unwrapElements(applicant1DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }

    @Test
    void addEventAndDateAddedToApplicantWitnesses_2v1_SingleResponseUnspec() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1ProceedWithClaimMultiParty2v1(YES)
            .applicant2ProceedWithClaimMultiParty2v1(YES)
            .addApplicant1ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToApplicantWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses applicant1DQWitnesses = updatedCaseData.getApplicant1DQ().getApplicant1DQWitnesses();
        Witnesses applicant2DQWitnesses = updatedCaseData.getApplicant2DQ().getApplicant2DQWitnesses();

        for (Witness witness : unwrapElements(applicant1DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }

        for (Witness witness : unwrapElements(applicant2DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }

    @Test
    void addEventAndDateAddedToApplicantWitnesses_2v1_DivergentResponseUnspec() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .addApplicant2ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToApplicantWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses applicant2DQWitnesses = updatedCaseData.getApplicant2DQ().getApplicant2DQWitnesses();

        assertThat(caseData.getApplicant1DQ()).isNull();

        for (Witness witness : unwrapElements(applicant2DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getApplicant2ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }

    @Test
    void addEventAndDateAddedToApplicantWitnesses_2v1_SingleResponseSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .multiPartyClaimTwoApplicants()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1ProceedWithClaimSpec2v1(YES)
            .addApplicant1ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToApplicantWitnesses(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Witnesses applicant1DQWitnesses = updatedCaseData.getApplicant1DQ().getApplicant1DQWitnesses();
        Witnesses applicant2DQWitnesses = updatedCaseData.getApplicant2DQ().getApplicant2DQWitnesses();

        for (Witness witness : unwrapElements(applicant1DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }

        for (Witness witness : unwrapElements(applicant2DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }
}
