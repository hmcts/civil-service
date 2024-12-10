package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentTwoSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

@Component
public class RespondentDocumentUploadTask extends DocumentUploadTask<RespondentSolicitorOneDocumentHandler, RespondentTwoSolicitorDocumentHandler> {

    public RespondentDocumentUploadTask(FeatureToggleService featureToggleService, ObjectMapper objectMapper,
                                        List<RespondentSolicitorOneDocumentHandler> respondentSolicitorOneDocumentHandlers,
                                        List<RespondentTwoSolicitorDocumentHandler> respondentSolicitorTwoDocumentHandlers) {
        super(featureToggleService, objectMapper, respondentSolicitorOneDocumentHandlers, respondentSolicitorTwoDocumentHandlers);
    }

    @Override
    void applyDocumentUploadDate(CaseData.CaseDataBuilder caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDateRes(now);
    }

    @Override
    protected String getSolicitorOneRole() {
        return RESPONDENTSOLICITORONE.name();
    }

    @Override
    String getSolicitorTwoRole() {
        return RESPONDENTSOLICITORTWO.name();
    }

    @Override
    protected String getSelectedValueForBoth() {
        return "RESPONDENTBOTH";
    }

    @Override
    protected String getLegalRepresentativeTypeString(String selectedRole) {
        String defendantString = "";
        if (selectedRole.equals(getSolicitorOneRole())) {
            return "Defendant 1";
        } else if (selectedRole.equals(getSolicitorTwoRole())) {
            return "Defendant 2";
        }
        if (selectedRole.equals(getSelectedValueForBoth())) {
            return "Both defendants";
        }
        return defendantString;
    }
}
