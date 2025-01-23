package uk.gov.hmcts.reform.civil.notification;

import uk.gov.hmcts.reform.civil.notify.NotificationService;

public abstract class EmailNotification {

    protected NotificationService notificationService;

    public abstract void notifyParties(EmailTO emailTO);

    public void notifyApplicantSolicitor1(EmailTO emailTO) {
        if (emailTO.getCaseData().getApplicantSolicitor1CheckEmail().isCorrect())
            notificationService.sendMail(emailTO.getApplicantSol1Email(), emailTO.getEmailTemplate(), emailTO.getApplicantSol1Params(),
                    emailTO.getApplicantRef());
    }

    public void notifyRespondentSolicitor1(EmailTO emailOneVOneTO) {
        if (!emailOneVOneTO.getCaseData().getRespondentSolicitor1EmailAddress().isBlank())
            notificationService.sendMail(emailOneVOneTO.getApplicantSol1Email(), emailOneVOneTO.getEmailTemplate(), emailOneVOneTO.getApplicantSol1Params(),
                    emailOneVOneTO.getApplicantRef());
    }
}
