package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import java.util.List;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.civil.handler.migration.PartyDataMigrationUtils.defaultIfNull;
import static uk.gov.hmcts.reform.civil.handler.migration.PartyDataMigrationUtils.generatePartyIdIfNull;
import static uk.gov.hmcts.reform.civil.handler.migration.PartyDataMigrationUtils.updateElements;

@Component
public class UpdatePartyWitnessTask extends MigrationTask<CaseReference> {

    public UpdatePartyWitnessTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getEventDescription() {
        return "This task UpdatePartyWitnessTask updates witness on the case";
    }

    @Override
    protected String getTaskName() {
        return "UpdatePartyWitnessTask";
    }

    @Override
    protected String getEventSummary() {
        return "Update case party witness via migration task";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseRef) {
        if (caseRef == null || caseRef.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }

        UnaryOperator<PartyFlagStructure> updatePartyFlag = witness -> witness.toBuilder()
            .firstName(defaultIfNull(witness.getFirstName()))
            .lastName(defaultIfNull(witness.getLastName()))
            .partyID(generatePartyIdIfNull(witness.getPartyID()))
            .build();

        UnaryOperator<Witness> updateWitness = witness -> witness.toBuilder()
            .firstName(defaultIfNull(witness.getFirstName()))
            .lastName(defaultIfNull(witness.getLastName()))
            .partyID(generatePartyIdIfNull(witness.getPartyID()))
            .build();

        // Update applicant witness
        List<Element<PartyFlagStructure>> updatedApplicantWitness =
            updateElements(caseData.getApplicantWitnesses(), updatePartyFlag);

        //Update respondent witness
        List<Element<PartyFlagStructure>> updatedRespondent1Witness =
            updateElements(caseData.getRespondent1Witnesses(), updatePartyFlag);

        List<Element<PartyFlagStructure>> updatedRespondent2Witness =
            updateElements(caseData.getRespondent2Witnesses(), updatePartyFlag);

        // Update DQ witness separately
        Applicant1DQ updatedApplicant1DQ = updateApplicant1DQWitness(caseData.getApplicant1DQ(), updateWitness);
        Applicant2DQ updatedApplicant2DQ = updateApplicant2DQWitness(caseData.getApplicant2DQ(), updateWitness);

        Respondent1DQ updateRespondent1DQ = updateRespondent1DQWitness(caseData.getRespondent1DQ(), updateWitness);
        Respondent2DQ updateRespondent2DQ = updateRespondent2DQWitness(caseData.getRespondent2DQ(), updateWitness);

        return caseData.toBuilder()
            .applicantWitnesses(updatedApplicantWitness)
            .respondent1Witnesses(updatedRespondent1Witness)
            .respondent2Witnesses(updatedRespondent2Witness)
            .applicant1DQ(updatedApplicant1DQ)
            .applicant2DQ(updatedApplicant2DQ)
            .respondent1DQ(updateRespondent1DQ)
            .respondent2DQ(updateRespondent2DQ)
            .build();
    }

    /**
     * Update Applicant1DQ witness.
     */
    private Applicant1DQ updateApplicant1DQWitness(Applicant1DQ dq, UnaryOperator<Witness> transformer) {
        if (dq == null || dq.getApplicant1DQWitnesses() == null) {
            return dq;
        }

        Witnesses updatedWitness = dq.getApplicant1DQWitnesses().toBuilder()
            .details(updateElements(dq.getApplicant1DQWitnesses().getDetails(), transformer))
            .build();

        return dq.toBuilder()
            .applicant1DQWitnesses(updatedWitness)
            .build();
    }

    private Respondent1DQ updateRespondent1DQWitness(Respondent1DQ dq, UnaryOperator<Witness> transformer) {
        if (dq == null || dq.getRespondent1DQWitnesses() == null) {
            return dq;
        }

        Witnesses updatedWitness = dq.getRespondent1DQWitnesses().toBuilder()
            .details(updateElements(dq.getRespondent1DQWitnesses().getDetails(), transformer))
            .build();

        return dq.toBuilder()
            .respondent1DQWitnesses(updatedWitness)
            .build();
    }

    private Respondent2DQ updateRespondent2DQWitness(Respondent2DQ dq, UnaryOperator<Witness> transformer) {
        if (dq == null || dq.getRespondent2DQWitnesses() == null) {
            return dq;
        }

        Witnesses updatedWitness = dq.getRespondent2DQWitnesses().toBuilder()
            .details(updateElements(dq.getRespondent2DQWitnesses().getDetails(), transformer))
            .build();

        return dq.toBuilder()
            .respondent2DQWitnesses(updatedWitness)
            .build();
    }

    /**
     * Update Applicant2DQ Witnesses.
     */
    private Applicant2DQ updateApplicant2DQWitness(Applicant2DQ dq, UnaryOperator<Witness> transformer) {
        if (dq == null || dq.getApplicant2DQWitnesses() == null) {
            return dq;
        }

        Witnesses updatedWitness = dq.getApplicant2DQWitnesses().toBuilder()
            .details(updateElements(dq.getApplicant2DQWitnesses().getDetails(), transformer))
            .build();

        return dq.toBuilder()
            .applicant2DQWitnesses(updatedWitness)
            .build();
    }
}
