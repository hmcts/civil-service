package uk.gov.hmcts.reform.civil.model.dq;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public interface DQ {

    FileDirectionsQuestionnaire getFileDirectionQuestionnaire();

    FixedRecoverableCosts getFixedRecoverableCosts();

    DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments();

    DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments();

    DisclosureReport getDisclosureReport();

    ExpertDetails getSmallClaimExperts();

    Experts getExperts();

    default Experts getExperts(Experts experts) {
        if (experts != null && experts.getExpertRequired() != null
            && experts.getExpertRequired().equals(NO)) {
            return experts.toBuilder().details(null).build();
        }
        return experts;
    }

    Witnesses getWitnesses();

    default Witnesses getWitnesses(Witnesses witnesses) {
        if (witnesses != null && witnesses.getWitnessesToAppear() != null
            && witnesses.getWitnessesToAppear().equals(NO)) {
            return witnesses.toBuilder().details(null).build();
        }
        return witnesses;
    }

    Hearing getHearing();

    default Hearing getHearing(Hearing hearing) {
        if (hearing != null && hearing.getUnavailableDatesRequired() != null
            && hearing.getUnavailableDatesRequired().equals(NO)) {
            return hearing.toBuilder().unavailableDates(null).build();
        }
        return hearing;
    }

    SmallClaimHearing getSmallClaimHearing();

    default SmallClaimHearing getSmallClaimHearing(SmallClaimHearing smallClaimHearing) {
        if (smallClaimHearing != null && smallClaimHearing.getUnavailableDatesRequired() != null
            && smallClaimHearing.getUnavailableDatesRequired().equals(NO)) {
            return smallClaimHearing.toBuilder().smallClaimUnavailableDate(null).build();
        }
        return smallClaimHearing;
    }

    Document getDraftDirections();

    RequestedCourt getRequestedCourt();

    HearingSupport getHearingSupport();

    FurtherInformation getFurtherInformation();

    RemoteHearing getRemoteHearing();

    WelshLanguageRequirements getWelshLanguageRequirements();

    RemoteHearingLRspec getRemoteHearingLRspec();

    WelshLanguageRequirements getWelshLanguageRequirementsLRspec();

    StatementOfTruth getStatementOfTruth();

    VulnerabilityQuestions getVulnerabilityQuestions();
}
