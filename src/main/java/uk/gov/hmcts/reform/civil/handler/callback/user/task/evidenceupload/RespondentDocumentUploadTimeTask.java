package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorTwoDocumentHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RespondentDocumentUploadTimeTask extends  DocumentUploadTimeTask {

    private final List<RespondentSolicitorOneDocumentHandler> respondentSolicitorOneDocumentHandlers;
    private final List<RespondentSolicitorTwoDocumentHandler> respondentSolicitorTwoDocumentHandlers;

    public RespondentDocumentUploadTimeTask(Time time, FeatureToggleService featureToggleService, ObjectMapper objectMapper, CaseDetailsConverter caseDetailsConverter, CoreCaseDataService coreCaseDataService,
                                            List<RespondentSolicitorOneDocumentHandler> respondentSolicitorOneDocumentHandlers,
                                            List<RespondentSolicitorTwoDocumentHandler> respondentSolicitorTwoDocumentHandlers) {
        super(time, featureToggleService, objectMapper, caseDetailsConverter, coreCaseDataService);
        this.respondentSolicitorOneDocumentHandlers = respondentSolicitorOneDocumentHandlers;
        this.respondentSolicitorTwoDocumentHandlers = respondentSolicitorTwoDocumentHandlers;
    }

    @Override
    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDateRes(now);
    }

    @Override
    void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData) {
            respondentSolicitorOneDocumentHandlers.forEach(handler -> handler.addUploadDocList(caseDataBuilder, caseData));
            respondentSolicitorTwoDocumentHandlers.forEach(handler -> handler.addUploadDocList(caseDataBuilder, caseData));
    }
}
