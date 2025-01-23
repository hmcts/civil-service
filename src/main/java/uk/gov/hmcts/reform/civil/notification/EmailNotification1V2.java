package uk.gov.hmcts.reform.civil.notification;

public class EmailNotification1V2 extends EmailNotification {

    @Override
    public void notifyParties(EmailTO emailTO) {
        notifyApplicantSolicitor1(emailTO);
        notifyRespondentSolicitor1(emailTO);
        notifyRespondentSolicitor2(emailTO);
    }

    public void notifyRespondentSolicitor2(EmailTO emailTO) {
        if (emailTO.getCanSendEmailToRespondentSol2() && !emailTO.getCaseData().getRespondentSolicitor2EmailAddress().isBlank()) {
            notificationService.sendMail(emailTO.getRespondentSol2Email(), emailTO.getEmailTemplate(), emailTO.getRespondentSol2Params(),
                    emailTO.getRespondentRef());
        }
    }
}
