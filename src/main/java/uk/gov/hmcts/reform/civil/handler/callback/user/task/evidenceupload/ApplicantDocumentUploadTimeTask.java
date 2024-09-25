package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorTwoDocumentHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ApplicantDocumentUploadTimeTask extends DocumentUploadTimeTask {

    private final List<ApplicantSolicitorOneDocumentHandler> applicantSolicitorOneDocumentHandlers;
    private final List<ApplicantSolicitorTwoDocumentHandler> applicantSolicitorTwoDocumentHandlers;

    public ApplicantDocumentUploadTimeTask(Time time, FeatureToggleService featureToggleService, ObjectMapper objectMapper,
                                           CaseDetailsConverter caseDetailsConverter, CoreCaseDataService coreCaseDataService,
                                           List<ApplicantSolicitorOneDocumentHandler> applicantSolicitorOneDocumentHandlers,
                                           List<ApplicantSolicitorTwoDocumentHandler> applicantSolicitorTwoDocumentHandlers) {
        super(time, featureToggleService, objectMapper, caseDetailsConverter, coreCaseDataService);
        this.applicantSolicitorOneDocumentHandlers = applicantSolicitorOneDocumentHandlers;
        this.applicantSolicitorTwoDocumentHandlers = applicantSolicitorTwoDocumentHandlers;
    }

    @Override
    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDate(now);
    }

    @Override
    void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData) {

        applicantSolicitorOneDocumentHandlers.forEach(handler -> handler.addUploadDocList(caseDataBuilder, caseData));
        applicantSolicitorTwoDocumentHandlers.forEach(handler -> handler.addUploadDocList(caseDataBuilder, caseData));
    }

}
