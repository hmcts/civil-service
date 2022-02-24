package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.aos.AcknowledgementOfClaimGeneratorForSpec;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC;

@Service
public class GenerateAcknowledgementOfClaimForSpecCallbackHandler
    extends AbstractGenerateAcknowledgementOfClaimCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC);
    private static final String TASK_ID = "AcknowledgeClaimGenerateAcknowledgementOfClaimForSpec";

    private final AcknowledgementOfClaimGeneratorForSpec acknowledgementOfClaimGenerator;

    public GenerateAcknowledgementOfClaimForSpecCallbackHandler(
        AcknowledgementOfClaimGeneratorForSpec acknowledgementOfClaimGenerator,
        ObjectMapper objectMapper) {
        super(objectMapper);
        this.acknowledgementOfClaimGenerator = acknowledgementOfClaimGenerator;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected CaseDocument generateAcknowledgementOfClaim(CaseData caseData, String bearerToken) {
        return acknowledgementOfClaimGenerator.generate(caseData, bearerToken);
    }
}
