package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.ApplicantDocumentUploadTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.ApplicantSetOptionsTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collections;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;

@Service
public class EvidenceUploadApplicantHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadApplicantHandler(UserService userService, CoreCaseUserService coreCaseUserService, ObjectMapper objectMapper,
                                          ApplicantSetOptionsTask setOptionsTask,
                                          ApplicantDocumentUploadTask documentUploadTimeTask) {
        super(userService, coreCaseUserService,
            objectMapper, Collections.singletonList(EVIDENCE_UPLOAD_APPLICANT),
            "validateValuesApplicant", "createShowCondition", setOptionsTask,
            documentUploadTimeTask
        );
    }

    @Override
    CallbackResponse createShowCondition(CaseData caseData, UserInfo userInfo) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        //For case which are 1v1, 2v1  we show respondent fields for documents to be uploaded,
        //if a case is 1v2 and different solicitors we want to show separate fields for each respondent solicitor i.e.
        //RESPONDENTSOLICITORTWO and RESPONDENTSOLICITORONE
        //if a case is 1v2 with same solicitor they will see respondent 2 fields as they have RESPONDENTSOLICITORTWO role
        //default flag for respondent 1 solicitor
        caseDataBuilder.caseTypeFlag("do_not_show");

        boolean multiParts = Objects.nonNull(caseData.getEvidenceUploadOptions())
            && !caseData.getEvidenceUploadOptions().getListItems().isEmpty();
        if (multiParts
            && caseData.getEvidenceUploadOptions()
            .getValue().getLabel().startsWith("Claimant 2 - ")) {
            caseDataBuilder.caseTypeFlag("ApplicantTwoFields");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(this.objectMapper))
            .build();
    }

    @Override
    CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData) {
        if (Objects.nonNull(caseData.getCaseTypeFlag())
            && caseData.getCaseTypeFlag().equals("ApplicantTwoFields")) {
            return validateValuesParty(caseData.getDocumentForDisclosureApp2(),
                caseData.getDocumentWitnessStatementApp2(),
                caseData.getDocumentWitnessSummaryApp2(),
                caseData.getDocumentHearsayNoticeApp2(),
                caseData.getDocumentReferredInStatementApp2(),
                caseData.getDocumentExpertReportApp2(),
                caseData.getDocumentJointStatementApp2(),
                caseData.getDocumentQuestionsApp2(),
                caseData.getDocumentAnswersApp2(),
                caseData.getDocumentEvidenceForTrialApp2(),
                caseData.getBundleEvidence());
        } else {
            return validateValuesParty(caseData.getDocumentForDisclosure(),
                caseData.getDocumentWitnessStatement(),
                caseData.getDocumentWitnessSummary(),
                caseData.getDocumentHearsayNotice(),
                caseData.getDocumentReferredInStatement(),
                caseData.getDocumentExpertReport(),
                caseData.getDocumentJointStatement(),
                caseData.getDocumentQuestions(),
                caseData.getDocumentAnswers(),
                caseData.getDocumentEvidenceForTrial(),
                caseData.getBundleEvidence());
        }
    }
}

