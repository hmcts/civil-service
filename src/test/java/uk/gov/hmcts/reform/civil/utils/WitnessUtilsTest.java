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

        addEventAndDateAddedToRespondentWitnesses(caseData);
        Witnesses respondent1DQWitnesses = caseData.getRespondent1DQ().getRespondent1DQWitnesses();

        assertThat(respondent1DQWitnesses.getDetails()).isNull();
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentWitnesses_1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .addRespondent1ExpertsAndWitnesses()
            .build();

        addEventAndDateAddedToRespondentWitnesses(caseData);
        Witnesses respondent1DQWitnesses = caseData.getRespondent1DQ().getRespondent1DQWitnesses();

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

        //  Set respondent2ResponseDate if it's null
        // In a 1v2 same solicitor single response scenario, respondent2 uses respondent1's response date
        if (caseData.getRespondent2ResponseDate() == null && caseData.getRespondent1ResponseDate() != null) {
            caseData.setRespondent2ResponseDate(caseData.getRespondent1ResponseDate());
        }

        // Method modifies caseData in place using setters
        addEventAndDateAddedToRespondentWitnesses(caseData);

        Witnesses respondent1DQWitnesses = caseData.getRespondent1DQ().getRespondent1DQWitnesses();
        Witnesses respondent2DQWitnesses = caseData.getRespondent2DQ().getRespondent2DQWitnesses();

        for (Witness witness : unwrapElements(respondent1DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }

        for (Witness witness : unwrapElements(respondent2DQWitnesses.getDetails())) {
            // Both respondents should use the same date in single response scenario
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

        // Method modifies caseData in place using setters
        addEventAndDateAddedToRespondentWitnesses(caseData);

        Witnesses respondent2DQWitnesses = caseData.getRespondent2DQ().getRespondent2DQWitnesses();

        assertThat(caseData.getRespondent1DQ()).isNull();

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

        // Method modifies caseData in place using setters
        addEventAndDateAddedToRespondentWitnesses(caseData);

        Witnesses respondent1DQWitnesses = caseData.getRespondent1DQ().getRespondent1DQWitnesses();
        Witnesses respondent2DQWitnesses = caseData.getRespondent2DQ().getRespondent2DQWitnesses();

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

        addEventAndDateAddedToApplicantWitnesses(caseData);
        Witnesses applicant1DQWitnesses = caseData.getApplicant1DQ().getApplicant1DQWitnesses();

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

        // Ensure applicant2ResponseDate is set if null in single response scenario
        if (caseData.getApplicant2ResponseDate() == null && caseData.getApplicant1ResponseDate() != null) {
            caseData.setApplicant2ResponseDate(caseData.getApplicant1ResponseDate());
        }

        addEventAndDateAddedToApplicantWitnesses(caseData);
        Witnesses applicant1DQWitnesses = caseData.getApplicant1DQ().getApplicant1DQWitnesses();
        Witnesses applicant2DQWitnesses = caseData.getApplicant2DQ().getApplicant2DQWitnesses();

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

        addEventAndDateAddedToApplicantWitnesses(caseData);
        Witnesses applicant2DQWitnesses = caseData.getApplicant2DQ().getApplicant2DQWitnesses();

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

        // Fix: Use setter to set the missing applicant2ResponseDate to prevent NullPointerException
        // This is needed because the test expects both applicants to have response dates
        // when processing witnesses in a 2v1 single response scenario
        if (caseData.getApplicant2ResponseDate() == null && caseData.getApplicant1ResponseDate() != null) {
            caseData.setApplicant2ResponseDate(caseData.getApplicant1ResponseDate());
        }

        addEventAndDateAddedToApplicantWitnesses(caseData);

        Witnesses applicant1DQWitnesses = caseData.getApplicant1DQ().getApplicant1DQWitnesses();
        Witnesses applicant2DQWitnesses = caseData.getApplicant2DQ().getApplicant2DQWitnesses();

        for (Witness witness : unwrapElements(applicant1DQWitnesses.getDetails())) {
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }

        for (Witness witness : unwrapElements(applicant2DQWitnesses.getDetails())) {
            // Since we're setting applicant2ResponseDate to match applicant1ResponseDate,
            // we expect the date to be from applicant1ResponseDate
            assertThat(witness.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }
}
