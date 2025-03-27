package uk.gov.hmcts.reform.civil.handler.message;

import uk.gov.hmcts.reform.civil.model.Result;
import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;

import java.util.List;

public interface CcdEventMessageHandler {

    boolean canHandle(String caseEvent);

    Result handle(String caseId, List<String> actions);
}
