package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.EventAddedEvents;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToApplicantExperts;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;

class ExpertUtilsTest {

    @Test
    void shouldNotAddEventAndDateAddedToRespondentExperts_1v1WhenNoExpertsExist() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts respondent1DQExperts = updatedCaseData.getRespondent1DQ().getRespondent1DQExperts();

        assertThat(respondent1DQExperts.getDetails()).isNull();
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentExperts_1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .addRespondent1ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts respondent1DQExperts = updatedCaseData.getRespondent1DQ().getRespondent1DQExperts();

        for (Expert expert : unwrapElements(respondent1DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentExperts_1v2SSSingleResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
            .addRespondent1ExpertsAndWitnesses()
            .respondentResponseIsSame(YES)
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts respondent1DQExperts = updatedCaseData.getRespondent1DQ().getRespondent1DQExperts();
        Experts respondent2DQExperts = updatedCaseData.getRespondent2DQ().getRespondent2DQExperts();

        for (Expert expert : unwrapElements(respondent1DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }

        for (Expert expert : unwrapElements(respondent2DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentExperts_1v2SSDivergentResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateDivergentResponseWithRespondent2FullDefence1v2SameSol_NotSingleDQ()
            .addRespondent2ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts respondent2DQExperts = updatedCaseData.getRespondent2DQ().getRespondent2DQExperts();

        assertThat(updatedCaseData.getRespondent1DQ()).isNull();

        for (Expert expert : unwrapElements(respondent2DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getRespondent2ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }
    }

    @Test
    void shouldAddEventAndDateAddedToRespondentExperts_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateRespondentFullDefence()
            .respondent2Responds(RespondentResponseType.FULL_DEFENCE)
            .respondent2DQ()
            .addRespondent1ExpertsAndWitnesses()
            .addRespondent2ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToRespondentExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts respondent1DQExperts = updatedCaseData.getRespondent1DQ().getRespondent1DQExperts();
        Experts respondent2DQExperts = updatedCaseData.getRespondent2DQ().getRespondent2DQExperts();

        for (Expert expert : unwrapElements(respondent1DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getRespondent1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }

        for (Expert expert : unwrapElements(respondent2DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getRespondent2ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.DEFENDANT_RESPONSE_EVENT.getValue());
        }
    }

    @Test
    void addEventAndDateAddedToApplicantExperts_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .addApplicant1ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToApplicantExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts applicant1DQExperts = updatedCaseData.getApplicant1DQ().getApplicant1DQExperts();

        for (Expert expert : unwrapElements(applicant1DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }

    @Test
    void addEventAndDateAddedToApplicantExperts_2v1_SingleResponseUnspec() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1ProceedWithClaimMultiParty2v1(YES)
            .applicant2ProceedWithClaimMultiParty2v1(YES)
            .addApplicant1ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToApplicantExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts applicant1DQExperts = updatedCaseData.getApplicant1DQ().getApplicant1DQExperts();
        Experts applicant2DQExperts = updatedCaseData.getApplicant2DQ().getApplicant2DQExperts();

        for (Expert expert : unwrapElements(applicant1DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }

        for (Expert expert : unwrapElements(applicant2DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }

    @Test
    void addEventAndDateAddedToApplicantExperts_2v1_DivergentResponseUnspec() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .addApplicant2ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToApplicantExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts applicant2DQExperts = updatedCaseData.getApplicant2DQ().getApplicant2DQExperts();

        assertThat(caseData.getApplicant1DQ()).isNull();

        for (Expert expert : unwrapElements(applicant2DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getApplicant2ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }

    @Test
    void addEventAndDateAddedToApplicantExperts_2v1_SingleResponseSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .multiPartyClaimTwoApplicants()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1ProceedWithClaimSpec2v1(YES)
            .addApplicant1ExpertsAndWitnesses()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        addEventAndDateAddedToApplicantExperts(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        Experts applicant1DQExperts = updatedCaseData.getApplicant1DQ().getApplicant1DQExperts();
        Experts applicant2DQExperts = updatedCaseData.getApplicant2DQ().getApplicant2DQExperts();

        for (Expert expert : unwrapElements(applicant1DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }

        for (Expert expert : unwrapElements(applicant2DQExperts.getDetails())) {
            assertThat(expert.getDateAdded()).isEqualTo(caseData.getApplicant1ResponseDate().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(EventAddedEvents.CLAIMANT_INTENTION_EVENT.getValue());
        }
    }
}
