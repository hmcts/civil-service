package uk.gov.hmcts.reform.civil.handler.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;


@Component
@Slf4j
@RequiredArgsConstructor
public class AddCaseNoteMessageHandler implements CcdEventMessageHandler {

    @Override
    public boolean canHandle(String caseEvent) {
        return ADD_CASE_NOTE.name().equals(caseEvent);
    }

    @Override
    public void handle(CcdEventMessage message) {

    }
}
