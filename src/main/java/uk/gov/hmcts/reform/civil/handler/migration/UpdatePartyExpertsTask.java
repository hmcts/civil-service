package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;

import java.util.List;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.civil.handler.migration.PartyDataMigrationUtils.defaultIfNull;
import static uk.gov.hmcts.reform.civil.handler.migration.PartyDataMigrationUtils.generatePartyIdIfNull;
import static uk.gov.hmcts.reform.civil.handler.migration.PartyDataMigrationUtils.updateElements;

@Component
public class UpdatePartyExpertsTask extends MigrationTask<CaseReference> {

    public UpdatePartyExpertsTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getEventSummary() {
        return "Update case party experts via migration task";
    }

    @Override
    protected String getTaskName() {
        return "UpdatePartyExpertsTask";
    }

    @Override
    protected String getEventDescription() {
        return "This task UpdatePartyExpertsTask updates experts on the case";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseRef) {
        if (caseRef == null || caseRef.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }

        UnaryOperator<PartyFlagStructure> updatePartyFlag = expert -> expert.copy()
            .setFirstName(defaultIfNull(expert.getFirstName()))
            .setLastName(defaultIfNull(expert.getLastName()))
            .setPartyID(generatePartyIdIfNull(expert.getPartyID()));

        UnaryOperator<Expert> updateExpert = expert -> expert.copy()
            .setFirstName(defaultIfNull(expert.getFirstName()))
            .setLastName(defaultIfNull(expert.getLastName()))
            .setPartyID(generatePartyIdIfNull(expert.getPartyID()));

        // Update applicant experts
        List<Element<PartyFlagStructure>> updatedApplicantExperts =
            updateElements(caseData.getApplicantExperts(), updatePartyFlag);

        //Update respondent experts
        List<Element<PartyFlagStructure>> updatedRespondent1Experts =
            updateElements(caseData.getRespondent1Experts(), updatePartyFlag);

        List<Element<PartyFlagStructure>> updatedRespondent2Experts =
            updateElements(caseData.getRespondent2Experts(), updatePartyFlag);

        // Update DQ experts separately
        Applicant1DQ updatedApplicant1DQ = updateApplicant1DQExperts(caseData.getApplicant1DQ(), updateExpert);
        Applicant2DQ updatedApplicant2DQ = updateApplicant2DQExperts(caseData.getApplicant2DQ(), updateExpert);

        Respondent1DQ updateRespondent1DQ = updateRespondent1DQExperts(caseData.getRespondent1DQ(), updateExpert);
        Respondent2DQ updateRespondent2DQ = updateRespondent2DQExperts(caseData.getRespondent2DQ(), updateExpert);

        return caseData.toBuilder()
            .applicantExperts(updatedApplicantExperts)
            .respondent1Experts(updatedRespondent1Experts)
            .respondent2Experts(updatedRespondent2Experts)
            .applicant1DQ(updatedApplicant1DQ)
            .applicant2DQ(updatedApplicant2DQ)
            .respondent1DQ(updateRespondent1DQ)
            .respondent2DQ(updateRespondent2DQ)
            .build();
    }

    /** Update Applicant1DQ expert. */
    private Applicant1DQ updateApplicant1DQExperts(Applicant1DQ dq, UnaryOperator<Expert> transformer) {
        if (dq == null || dq.getApplicant1DQExperts() == null) {
            return dq;
        }

        Experts updatedExperts = dq.getApplicant1DQExperts().copy()
            .setDetails(updateElements(dq.getApplicant1DQExperts().getDetails(), transformer));

        return dq.copy()
            .setApplicant1DQExperts(updatedExperts);
    }

    private Respondent1DQ updateRespondent1DQExperts(Respondent1DQ dq, UnaryOperator<Expert> transformer) {
        if (dq == null || dq.getRespondent1DQExperts() == null) {
            return dq;
        }

        Experts updatedExperts = dq.getRespondent1DQExperts().copy()
            .setDetails(updateElements(dq.getRespondent1DQExperts().getDetails(), transformer));

        return dq.copy()
            .setRespondent1DQExperts(updatedExperts);
    }

    private Respondent2DQ updateRespondent2DQExperts(Respondent2DQ dq, UnaryOperator<Expert> transformer) {
        if (dq == null || dq.getRespondent2DQExperts() == null) {
            return dq;
        }

        Experts updatedExperts = dq.getRespondent2DQExperts().copy()
            .setDetails(updateElements(dq.getRespondent2DQExperts().getDetails(), transformer));

        return dq.copy()
            .setRespondent2DQExperts(updatedExperts);
    }

    /** Update Applicant2DQ experts. */
    private Applicant2DQ updateApplicant2DQExperts(Applicant2DQ dq, UnaryOperator<Expert> transformer) {
        if (dq == null || dq.getApplicant2DQExperts() == null) {
            return dq;
        }

        Experts updatedExperts = dq.getApplicant2DQExperts().copy()
            .setDetails(updateElements(dq.getApplicant2DQExperts().getDetails(), transformer));

        return dq.copy()
            .setApplicant2DQExperts(updatedExperts);
    }
}
