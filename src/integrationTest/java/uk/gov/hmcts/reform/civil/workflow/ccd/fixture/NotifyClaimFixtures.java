package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CALLBACK_TIME;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.RESPONDENT_ONE_ORGANISATION;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.RESPONDENT_TWO_ORGANISATION;

public final class NotifyClaimFixtures {

    private NotifyClaimFixtures() {
    }

    public static CaseData issuedRepresentedOneVOneClaim() {
        return ClaimLifecycleFixtures.withIssuedLifecycleDates(CaseDataBuilder.builder()
            .atStateClaimIssued()
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.clearedPolicy(RESPONDENTSOLICITORONE))
            .respondent1OrganisationIDCopy(RESPONDENT_ONE_ORGANISATION)
            .defendant1LIPAtClaimIssued(NO)
            .claimNotificationDeadline(CALLBACK_TIME.plusMonths(4))
            .build());
    }

    public static CaseData issuedTwoSolicitorClaim(String selectedOption) {
        CaseDataBuilder builder = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.clearedPolicy(RESPONDENTSOLICITORONE))
            .respondent2OrganisationPolicy(ClaimLifecycleFixtures.clearedPolicy(RESPONDENTSOLICITORTWO))
            .respondent1OrganisationIDCopy(RESPONDENT_ONE_ORGANISATION)
            .respondent2OrganisationIDCopy(RESPONDENT_TWO_ORGANISATION)
            .defendant1LIPAtClaimIssued(NO)
            .defendant2LIPAtClaimIssued(NO)
            .claimNotificationDeadline(CALLBACK_TIME.plusMonths(4));

        if (selectedOption != null) {
            builder.defendantSolicitorNotifyClaimOptions(selectedOption);
        }

        return ClaimLifecycleFixtures.withIssuedLifecycleDates(builder.build());
    }

    public static CaseData issuedSameSolicitorClaim() {
        return issuedTwoSolicitorClaim(null).toBuilder()
            .respondent2SameLegalRepresentative(YES)
            .respondent2OrganisationIDCopy(RESPONDENT_ONE_ORGANISATION)
            .build();
    }

    public static CaseData issuedMixedRepresentationClaim(CertificateOfService respondentTwoCertificate) {
        return ClaimLifecycleFixtures.withIssuedLifecycleDates(CaseDataBuilder.builder()
            .atStateClaimIssued()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2Represented(NO)
            .respondent2OrgRegistered(NO)
            .respondent2OrganisationPolicy(ClaimLifecycleFixtures.clearedPolicy(RESPONDENTSOLICITORTWO))
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.clearedPolicy(RESPONDENTSOLICITORONE))
            .respondent1OrganisationIDCopy(RESPONDENT_ONE_ORGANISATION)
            .defendant1LIPAtClaimIssued(NO)
            .defendant2LIPAtClaimIssued(YES)
            .cosNotifyClaimDefendant2(respondentTwoCertificate)
            .claimNotificationDeadline(CALLBACK_TIME.plusMonths(4))
            .build());
    }

    public static CaseData issuedOneVOneLipClaim(CertificateOfService respondentOneCertificate) {
        return ClaimLifecycleFixtures.withIssuedLifecycleDates(CaseDataBuilder.builder()
            .atStateClaimIssued()
            .respondent1Represented(NO)
            .respondent1OrgRegistered(null)
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.clearedPolicy(RESPONDENTSOLICITORONE))
            .defendant1LIPAtClaimIssued(YES)
            .cosNotifyClaimDefendant1(respondentOneCertificate)
            .claimNotificationDeadline(CALLBACK_TIME.plusMonths(4))
            .build());
    }

    public static CaseData issuedTwoLipClaim(
        CertificateOfService respondentOneCertificate,
        CertificateOfService respondentTwoCertificate
    ) {
        return ClaimLifecycleFixtures.withIssuedLifecycleDates(CaseDataBuilder.builder()
            .atStateClaimIssued()
            .multiPartyClaimTwoDefendantsLiP()
            .respondent1OrgRegistered(null)
            .respondent2OrgRegistered(null)
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.clearedPolicy(RESPONDENTSOLICITORONE))
            .respondent2OrganisationPolicy(ClaimLifecycleFixtures.clearedPolicy(RESPONDENTSOLICITORTWO))
            .defendant1LIPAtClaimIssued(YES)
            .defendant2LIPAtClaimIssued(YES)
            .cosNotifyClaimDefendant1(respondentOneCertificate)
            .cosNotifyClaimDefendant2(respondentTwoCertificate)
            .claimNotificationDeadline(CALLBACK_TIME.plusMonths(4))
            .build());
    }
}
