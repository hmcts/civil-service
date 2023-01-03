package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;

@Service
public class EvidenceUploadRespondentHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadRespondentHandler(UserService userService, CoreCaseUserService coreCaseUserService, ObjectMapper objectMapper, Time time) {
        super(userService, coreCaseUserService, objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_RESPONDENT),
              "validateValuesRespondent");
    }

}

