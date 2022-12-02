package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;

@Service
public class EvidenceUploadApplicantHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadApplicantHandler(UserService userService, CoreCaseUserService coreCaseUserService, ObjectMapper objectMapper, Time time) {
        super(userService, coreCaseUserService, objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_APPLICANT), "validateValuesApplicant");
    }

    @Override
    CallbackResponse caseType(CaseData caseData, CallbackParams callbackParams) {
        return caseTypeDetermine(caseData, callbackParams);
    }

    @Override
    CallbackResponse validateValues(CaseData caseData) {
        return validateValuesParty(caseData.getDocumentWitnessStatement(),
                                   caseData.getDocumentHearsayNotice(),
                                   caseData.getDocumentExpertReport(),
                                   caseData.getDocumentJointStatement(),
                                   caseData.getDocumentQuestions(),
                                   caseData.getDocumentAnswers());

    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDate(now);
    }
}

