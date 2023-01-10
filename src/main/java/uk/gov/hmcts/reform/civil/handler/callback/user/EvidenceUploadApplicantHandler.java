package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;

@Service
public class EvidenceUploadApplicantHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadApplicantHandler(UserService userService, CoreCaseUserService coreCaseUserService, ObjectMapper objectMapper, Time time) {
        super(userService, coreCaseUserService, objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_APPLICANT),
              "validateValuesApplicant", null);
    }

    @Override
    CallbackResponse createShowCondition(CaseData caseData) {
        return null;
    }

    @Override
    CallbackResponse validateValues(CaseData caseData) {
        return validateValuesParty(caseData.getDocumentWitnessStatement(),
                                   caseData.getDocumentHearsayNotice(),
                                   caseData.getDocumentExpertReport(),
                                   caseData.getDocumentJointStatement(),
                                   caseData.getDocumentQuestions(),
                                   caseData.getDocumentAnswers(),
                                   caseData.getDocumentWitnessStatementRes2(),
                                   caseData.getDocumentHearsayNoticeRes2(),
                                   caseData.getDocumentExpertReportRes2(),
                                   caseData.getDocumentJointStatementRes2(),
                                   caseData.getDocumentQuestionsRes2(),
                                   caseData.getDocumentAnswersRes2(),
                                   caseData.getDocumentForDisclosure(),
                                   caseData.getDocumentForDisclosureRes2(),
                                   caseData.getDocumentReferredInStatement(),
                                   caseData.getDocumentReferredInStatementRes2(),
                                   caseData.getDocumentEvidenceForTrial(),
                                   caseData.getDocumentEvidenceForTrialRes2());
    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDate(now);
    }
}

