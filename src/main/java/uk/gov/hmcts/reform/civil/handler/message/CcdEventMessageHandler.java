package uk.gov.hmcts.reform.civil.handler.message;

import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;

public interface CcdEventMessageHandler {

    boolean canHandle(String caseEvent);

    void handle(CcdEventMessage message);
}
