package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent;

import org.jetbrains.annotations.Nullable;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

public abstract class CaseHandedOfflineRespondentNotifierBase implements NotificationData {

    protected static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-respondent-notification-%s";

    @Nullable
    protected String getRecipientEmailAddress(CaseData caseData,
                                              CaseHandledOfflineRecipient recipientType) {
        String recipientEmailAddress;
        if (is1v1Or2v1Case(caseData)) {
            recipientEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
        } else {
            if (recipientType.equals(DefendantResponseCaseHandedOfflineRespondentNotifier.CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR1)) {
                recipientEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
            } else {
                recipientEmailAddress = caseData.getRespondentSolicitor2EmailAddress();
            }

            if (null == recipientEmailAddress && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
                recipientEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
            }
        }
        return recipientEmailAddress;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return NotificationUtils.caseOfflineNotificationAddProperties(caseData);
    }

    abstract void notifyRespondentSolicitorForCaseHandedOffline(CaseData caseData,
                                                                       CaseHandledOfflineRecipient recipientType);
}
