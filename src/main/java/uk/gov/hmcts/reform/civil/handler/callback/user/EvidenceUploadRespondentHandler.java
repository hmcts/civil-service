package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;

@Service
public class EvidenceUploadRespondentHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadRespondentHandler(ObjectMapper objectMapper, Time time) {
        super(objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_RESPONDENT), "validateValuesRespondent");
    }

    @Override
    CallbackResponse validateValues(CaseData caseData) {
        return validateValuesParty(caseData.getDocumentUploadWitness1Res(),
                                   caseData.getDocumentUploadWitness3Res(),
                                   caseData.getDocumentUploadExpert1Res(),
                                   caseData.getDocumentUploadExpert2Res(),
                                   caseData.getDocumentUploadExpert3Res(),
                                   caseData.getDocumentUploadExpert4Res());

    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDateRes(now);
    }
}

