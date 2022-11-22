package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_OTHER_PARTY;

@Service
public class EvidenceUploadOtherPartyHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadOtherPartyHandler(ObjectMapper objectMapper, Time time) {
        super(objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_OTHER_PARTY), "validateValuesOtherParty");
    }

    @Override
    CallbackResponse validateValues(CaseData caseData) {
        return validateValuesParty(caseData.getDocumentUploadWitness1Other(),
                                   caseData.getDocumentUploadWitness3Other(),
                                   caseData.getDocumentUploadExpert1Other(),
                                   caseData.getDocumentUploadExpert2Other(),
                                   caseData.getDocumentUploadExpert3Other(),
                                   caseData.getDocumentUploadExpert4Other());

    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDateOther(now);
    }
}

