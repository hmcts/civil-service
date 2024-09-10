package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceuploadhandlerbasetask.DocumentUploadTimeTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceuploadhandlerbasetask.SetOptionsTask;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;

@Service
public class EvidenceUploadRespondentHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadRespondentHandler(UserService userService, CoreCaseUserService coreCaseUserService,
                                           CaseDetailsConverter caseDetailsConverter,
                                           CoreCaseDataService coreCaseDataService,
                                           ObjectMapper objectMapper, Time time, FeatureToggleService featureToggleService,
                                           SetOptionsTask setOptionsTask, DocumentUploadTimeTask documentUploadTimeTask) {
        super(userService, coreCaseUserService, caseDetailsConverter, coreCaseDataService,
              objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_RESPONDENT),
              "validateValuesRespondent", "createShowCondition", featureToggleService, setOptionsTask,
              documentUploadTimeTask
        );
    }

    @Override
    CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData) {
        if (Objects.nonNull(caseData.getCaseTypeFlag())
                && caseData.getCaseTypeFlag().equals("RespondentTwoFields")) {
            return validateValuesParty(caseData.getDocumentForDisclosureRes2(),
                                       caseData.getDocumentWitnessStatementRes2(),
                                       caseData.getDocumentWitnessSummaryRes2(),
                                       caseData.getDocumentHearsayNoticeRes2(),
                                       caseData.getDocumentReferredInStatementRes2(),
                                       caseData.getDocumentExpertReportRes2(),
                                       caseData.getDocumentJointStatementRes2(),
                                       caseData.getDocumentQuestionsRes2(),
                                       caseData.getDocumentAnswersRes2(),
                                       caseData.getDocumentEvidenceForTrialRes2());
        } else {
            return validateValuesParty(caseData.getDocumentForDisclosureRes(),
                                       caseData.getDocumentWitnessStatementRes(),
                                       caseData.getDocumentWitnessSummaryRes(),
                                       caseData.getDocumentHearsayNoticeRes(),
                                       caseData.getDocumentReferredInStatementRes(),
                                       caseData.getDocumentExpertReportRes(),
                                       caseData.getDocumentJointStatementRes(),
                                       caseData.getDocumentQuestionsRes(),
                                       caseData.getDocumentAnswersRes(),
                                       caseData.getDocumentEvidenceForTrialRes());
        }
    }

    @Override
    CallbackResponse createShowCondition(CaseData caseData, UserInfo userInfo) {

        return showCondition(caseData, userInfo, caseData.getWitnessSelectionEvidenceRes(),
                             caseData.getWitnessSelectionEvidenceSmallClaimRes(),
                             caseData.getWitnessSelectionEvidenceRes(),
                             caseData.getWitnessSelectionEvidenceSmallClaimRes(),
                             caseData.getWitnessSelectionEvidenceRes(),
                             caseData.getWitnessSelectionEvidenceSmallClaimRes(),
                             caseData.getExpertSelectionEvidenceRes(),
                             caseData.getExpertSelectionEvidenceSmallClaimRes(),
                             caseData.getExpertSelectionEvidenceRes(),
                             caseData.getExpertSelectionEvidenceSmallClaimRes(),
                             caseData.getTrialSelectionEvidenceRes(),
                             caseData.getTrialSelectionEvidenceSmallClaimRes(),
                             caseData.getTrialSelectionEvidenceRes(),
                             caseData.getTrialSelectionEvidenceSmallClaimRes(),
                             caseData.getTrialSelectionEvidenceRes(),
                             caseData.getTrialSelectionEvidenceSmallClaimRes());
    }
}

