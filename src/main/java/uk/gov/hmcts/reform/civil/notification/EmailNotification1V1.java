package uk.gov.hmcts.reform.civil.notification;

public class EmailNotification1V1 extends EmailNotification {

    @Override
    public void notifyParties(EmailTO emailTO) {
        notifyApplicantSolicitor1(emailTO);
        notifyRespondentSolicitor1(emailTO);
    }
}
