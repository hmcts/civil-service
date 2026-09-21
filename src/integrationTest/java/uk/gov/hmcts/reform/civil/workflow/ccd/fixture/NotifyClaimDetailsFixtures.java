package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CALLBACK_TIME;

public final class NotifyClaimDetailsFixtures {

    private NotifyClaimDetailsFixtures() {
    }

    public static CaseData notifiedRepresentedOneVOneClaim() {
        return withClaimNotification(NotifyClaimFixtures.issuedRepresentedOneVOneClaim());
    }

    public static CaseData notifiedTwoSolicitorClaim(String selectedOption) {
        CaseData issued = NotifyClaimFixtures.issuedTwoSolicitorClaim(null);
        CaseData notified = withClaimNotification(issued);

        if (selectedOption == null) {
            return notified;
        }

        return notified.toBuilder()
            .defendantSolicitorNotifyClaimDetailsOptions(
                new DynamicList().setValue(new DynamicListElement().setLabel(selectedOption))
            )
            .build();
    }

    public static CaseData notifiedSameSolicitorClaim() {
        return withClaimNotification(NotifyClaimFixtures.issuedSameSolicitorClaim()).toBuilder()
            .defendantSolicitorNotifyClaimDetailsOptions(
                new DynamicList().setValue(new DynamicListElement().setLabel("Both"))
            )
            .build();
    }

    public static CaseData notifiedOneVOneLipClaim(CertificateOfService respondentOneCertificate) {
        return withClaimNotification(NotifyClaimFixtures.issuedOneVOneLipClaim(null)).toBuilder()
            .cosNotifyClaimDetails1(respondentOneCertificate)
            .build();
    }

    public static CaseData notifiedTwoLipClaim(
        CertificateOfService respondentOneCertificate,
        CertificateOfService respondentTwoCertificate
    ) {
        return withClaimNotification(NotifyClaimFixtures.issuedTwoLipClaim(null, null)).toBuilder()
            .cosNotifyClaimDetails1(respondentOneCertificate)
            .cosNotifyClaimDetails2(respondentTwoCertificate)
            .build();
    }

    public static CaseData notifiedMixedRepresentationClaim(
        CertificateOfService respondentOneCertificate,
        CertificateOfService respondentTwoCertificate
    ) {
        return withClaimNotification(NotifyClaimFixtures.issuedMixedRepresentationClaim(null)).toBuilder()
            .cosNotifyClaimDetails1(respondentOneCertificate)
            .cosNotifyClaimDetails2(respondentTwoCertificate)
            .build();
    }

    private static CaseData withClaimNotification(CaseData issuedClaim) {
        return issuedClaim.toBuilder()
            .claimNotificationDate(CALLBACK_TIME.minusDays(1))
            .claimDetailsNotificationDeadline(CALLBACK_TIME.plusDays(14))
            .servedDocumentFiles(new ServedDocumentFiles().setParticularsOfClaimText("Claim details"))
            .build();
    }
}
