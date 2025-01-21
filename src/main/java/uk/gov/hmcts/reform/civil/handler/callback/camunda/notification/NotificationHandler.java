package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
@RequiredArgsConstructor
public abstract class NotificationHandler extends CallbackHandler implements NotificationData {
    protected abstract CallbackResponse notifyParties(CallbackParams callbackParams);

    protected abstract void notifyApplicants(final CaseData caseData);

    protected abstract void notifyRespondents(final CaseData caseData);
}
