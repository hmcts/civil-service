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
    CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData) {
        return validateValuesParty(caseData.getDocumentForDisclosure(),
                                   caseData.getDocumentWitnessStatement(),
                                   caseData.getDocumentHearsayNotice(),
                                   caseData.getDocumentReferredInStatement(),
                                   caseData.getDocumentExpertReport(),
                                   caseData.getDocumentJointStatement(),
                                   caseData.getDocumentQuestions(),
                                   caseData.getDocumentAnswers(),
                                   caseData.getDocumentEvidenceForTrial());
    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDate(now);
    }
}

