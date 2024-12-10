package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantOneSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantTwoSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;

@Component
public class ApplicantDocumentUploadTask extends DocumentUploadTask<ApplicantOneSolicitorDocumentHandler, ApplicantTwoSolicitorDocumentHandler> {

    public ApplicantDocumentUploadTask(FeatureToggleService featureToggleService, ObjectMapper objectMapper,
                                       List<ApplicantOneSolicitorDocumentHandler> applicantSolicitorOneDocumentHandlers,
                                       List<ApplicantTwoSolicitorDocumentHandler> applicantSolicitorTwoDocumentHandlers) {
        super(featureToggleService, objectMapper, applicantSolicitorOneDocumentHandlers, applicantSolicitorTwoDocumentHandlers);
    }

    @Override
    void applyDocumentUploadDate(CaseData.CaseDataBuilder caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDate(now);
    }

    @Override
    protected String getSolicitorOneRole() {
        return APPLICANTSOLICITORONE.name();
    }

    @Override
    String getSolicitorTwoRole() {
        return "APPLICANTSOLICITORTWO";
    }

    @Override
    protected String getSelectedValueForBoth() {
        return "APPLICANTBOTH";
    }

    @Override
    protected String getLegalRepresentativeTypeString(String selectedRole) {
        String defendantString = "";
        if (selectedRole.equals(getSolicitorOneRole())) {
            return "Claimant 1";
        } else if (selectedRole.equals(getSolicitorTwoRole())) {
            return "Claimant 2";
        }
        if (selectedRole.equals(getSelectedValueForBoth())) {
            return "Both claimants";
        }
        return defendantString;
    }
}
