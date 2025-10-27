package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Experts;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

        // --- Update Applicant Experts ---
        List<Element<PartyFlagStructure>> updatedApplicantExperts =
            updateElements(caseData.getApplicantExperts(), expert -> expert.toBuilder()
                .firstName(expert.getFirstName() == null ? "TBC" : expert.getFirstName())
                .lastName(expert.getLastName() == null ? "TBC" : expert.getLastName())
                .build()
            );

        Applicant1DQ applicant1DQ = caseData.getApplicant1DQ();
        Applicant2DQ applicant2DQ = caseData.getApplicant2DQ();

        // --- Update Applicant 1 DQ Experts ---
        if (applicant1DQ != null && applicant1DQ.getApplicant1DQExperts() != null) {
            Experts updatedExperts = applicant1DQ.getApplicant1DQExperts().toBuilder()
                .details(updateElements(applicant1DQ.getApplicant1DQExperts().getDetails(),
                                        expert -> expert.toBuilder()
                                            .firstName(expert.getFirstName() == null ? "TBC" : expert.getFirstName())
                                            .lastName(expert.getLastName() == null ? "TBC" : expert.getLastName())
                                            .build()
                ))
                .build();
            applicant1DQ = applicant1DQ.toBuilder()
                .applicant1DQExperts(updatedExperts)
                .build();
        }

        // --- Update Applicant 2 DQ Experts ---
        if (applicant2DQ != null && applicant2DQ.getApplicant2DQExperts() != null) {
            Experts updatedExperts = applicant2DQ.getApplicant2DQExperts().toBuilder()
                .details(updateElements(applicant2DQ.getApplicant2DQExperts().getDetails(),
                                        expert -> expert.toBuilder()
                                            .firstName(expert.getFirstName() == null ? "TBC" : expert.getFirstName())
                                            .lastName(expert.getLastName() == null ? "TBC" : expert.getLastName())
                                            .build()
                ))
                .build();
            applicant2DQ = applicant2DQ.toBuilder()
                .applicant2DQExperts(updatedExperts)
                .build();
        }

        // --- Rebuild and return updated CaseData ---
        return caseData.toBuilder()
            .applicantExperts(updatedApplicantExperts)
            .applicant1DQ(applicant1DQ)
            .applicant2DQ(applicant2DQ)
            .build();
    }

    /**
     * Generic helper method to update elements using a provided transformer function.
     */
    private <T> List<Element<T>> updateElements(List<Element<T>> elements, Function<T, T> transformer) {
        return Optional.ofNullable(elements)
            .orElse(Collections.emptyList())
            .stream()
            .map(element -> Element.<T>builder()
                .id(element.getId())
                .value(transformer.apply(element.getValue()))
                .build())
            .toList();
    }
}
