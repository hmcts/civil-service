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
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Component
public class UpdateApplicantExpertsTask extends MigrationTask<CaseReference> {

    public UpdateApplicantExpertsTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getEventSummary() {
        return "Update case applicant1 experts via migration task";
    }

    @Override
    protected String getEventDescription() {
        return "This task update applicant1 experts on the case";
    }

    @Override
    protected String getTaskName() {
        return "UpdateApplicantExpertsTask";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseRef) {
        if (caseRef == null || caseRef.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }

        UnaryOperator<PartyFlagStructure> updatePartyFlag = expert -> expert.toBuilder()
            .firstName(defaultIfNull(expert.getFirstName()))
            .lastName(defaultIfNull(expert.getLastName()))
            .partyID(updatePartyId(expert.getPartyID()))
            .build();

        UnaryOperator<Expert> updateExpert = expert -> expert.toBuilder()
            .firstName(defaultIfNull(expert.getFirstName()))
            .lastName(defaultIfNull(expert.getLastName()))
            .partyID(updatePartyId(expert.getPartyID()))
            .build();

        // Update applicant experts
        List<Element<PartyFlagStructure>> updatedApplicantExperts =
            updateElements(caseData.getApplicantExperts(), updatePartyFlag);

        // Update DQ experts separately
        Applicant1DQ updatedApplicant1DQ = updateApplicant1DQExperts(caseData.getApplicant1DQ(), updateExpert);
        Applicant2DQ updatedApplicant2DQ = updateApplicant2DQExperts(caseData.getApplicant2DQ(), updateExpert);

        return caseData.toBuilder()
            .applicantExperts(updatedApplicantExperts)
            .applicant1DQ(updatedApplicant1DQ)
            .applicant2DQ(updatedApplicant2DQ)
            .build();
    }

    /** Update Applicant1DQ expert. */
    private Applicant1DQ updateApplicant1DQExperts(Applicant1DQ dq, UnaryOperator<Expert> transformer) {
        if (dq == null || dq.getApplicant1DQExperts() == null) {
            return dq;
        }

        Experts updatedExperts = dq.getApplicant1DQExperts().toBuilder()
            .details(updateElements(dq.getApplicant1DQExperts().getDetails(), transformer))
            .build();

        return dq.toBuilder()
            .applicant1DQExperts(updatedExperts)
            .build();
    }

    /** Update Applicant2DQ experts. */
    private Applicant2DQ updateApplicant2DQExperts(Applicant2DQ dq, UnaryOperator<Expert> transformer) {
        if (dq == null || dq.getApplicant2DQExperts() == null) {
            return dq;
        }

        Experts updatedExperts = dq.getApplicant2DQExperts().toBuilder()
            .details(updateElements(dq.getApplicant2DQExperts().getDetails(), transformer))
            .build();

        return dq.toBuilder()
            .applicant2DQExperts(updatedExperts)
            .build();
    }

    /** Update a list of elements using a UnaryOperator. */
    private <T> List<Element<T>> updateElements(List<Element<T>> elements, UnaryOperator<T> transformer) {
        return Optional.ofNullable(elements)
            .orElse(Collections.emptyList())
            .stream()
            .map(element -> Element.<T>builder()
                .id(element.getId())
                .value(transformer.apply(element.getValue()))
                .build())
            .toList();
    }

    /** Return "TBC" if null. */
    private String defaultIfNull(String value) {
        return value == null ? "TBC" : value;
    }

    private String updatePartyId(String value) {
        return value == null ? PartyUtils.createPartyId() : value;
    }
}
