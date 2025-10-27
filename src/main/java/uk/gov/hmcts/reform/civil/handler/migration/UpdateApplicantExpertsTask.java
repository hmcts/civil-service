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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseRef) {
        if (caseRef == null || caseRef.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }

        // --- Applicant Experts ---
        List<Element<PartyFlagStructure>> updatedApplicantExperts =
            Optional.ofNullable(caseData.getApplicantExperts())
                .orElse(Collections.emptyList())
                .stream()
                .peek(element -> {
                    PartyFlagStructure expert = element.getValue();
                    if (expert.getFirstName() == null) {
                        expert.setFirstName("TBC");
                    }
                    if (expert.getLastName() == null) {
                        expert.setLastName("TBC");
                    }
                })
                .toList();

        Applicant1DQ applicant1DQ = caseData.getApplicant1DQ();
        Applicant2DQ applicant2DQ = caseData.getApplicant2DQ();

        // --- Applicant 1 DQ Experts ---
        if (applicant1DQ != null && applicant1DQ.getApplicant1DQExperts() != null) {
            Experts applicant1DQExperts = applicant1DQ.getApplicant1DQExperts();

            List<Element<Expert>> updatedDQExperts =
                Optional.ofNullable(applicant1DQExperts.getDetails())
                    .orElse(Collections.emptyList())
                    .stream()
                    .peek(element -> {
                        Expert expert = element.getValue();
                        if (expert.getFirstName() == null) {
                            expert.setFirstName("TBC");
                        }
                        if (expert.getLastName() == null) {
                            expert.setLastName("TBC");
                        }
                    })
                    .toList();

            applicant1DQExperts = applicant1DQExperts.toBuilder()
                .details(updatedDQExperts)
                .build();

            applicant1DQ = applicant1DQ.toBuilder()
                .applicant1DQExperts(applicant1DQExperts)
                .build();
        }

        // --- Applicant 2 DQ Experts ---
        if (applicant2DQ != null && applicant2DQ.getApplicant2DQExperts() != null) {
            Experts applicant2DQExperts = applicant2DQ.getApplicant2DQExperts();

            List<Element<Expert>> updatedApplicant2DQExperts =
                Optional.ofNullable(applicant2DQExperts.getDetails())
                    .orElse(Collections.emptyList())
                    .stream()
                    .peek(element -> {
                        Expert expert = element.getValue();
                        if (expert.getFirstName() == null) {
                            expert.setFirstName("TBC");
                        }
                        if (expert.getLastName() == null) {
                            expert.setLastName("TBC");
                        }
                    })
                    .toList();

            applicant2DQExperts = applicant2DQExperts.toBuilder()
                .details(updatedApplicant2DQExperts)
                .build();

            applicant2DQ = applicant2DQ.toBuilder()
                .applicant2DQExperts(applicant2DQExperts)
                .build();
        }

        // --- Rebuild and return updated CaseData ---
        return caseData.toBuilder()
            .applicantExperts(updatedApplicantExperts)
            .applicant1DQ(applicant1DQ)
            .applicant2DQ(applicant2DQ)
            .build();
    }
}
