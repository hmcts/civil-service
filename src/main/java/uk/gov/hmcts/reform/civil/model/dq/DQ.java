package uk.gov.hmcts.reform.civil.model.dq;

import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public interface DQ {

    FileDirectionsQuestionnaire getFileDirectionQuestionnaire();

    DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments();

    DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments();

    DisclosureReport getDisclosureReport();

    Experts getExperts();

    default Experts getExperts(Experts experts) {
        if (ofNullable(experts).map(Experts::getExpertRequired).map(NO::equals).orElse(false)) {
            return experts.toBuilder().details(null).build();
        }
        return experts;
    }

    Witnesses getWitnesses();

    default Witnesses getWitnesses(Witnesses witnesses) {
        if (ofNullable(witnesses).map(Witnesses::getWitnessesToAppear).map(NO::equals).orElse(false)) {
            return witnesses.toBuilder().details(null).build();
        }
        return witnesses;
    }

    Hearing getHearing();

    default Hearing getHearing(Hearing hearing) {
        if (ofNullable(hearing).map(Hearing::getUnavailableDatesRequired).map(NO::equals).orElse(false)) {
            return hearing.toBuilder().unavailableDates(null).build();
        }
        return hearing;
    }

    SmallClaimHearing getSmallClaimHearing();

    default SmallClaimHearing getSmallClaimHearing(SmallClaimHearing smallClaimHearing) {
        if (ofNullable(smallClaimHearing)
            .map(SmallClaimHearing::getUnavailableDatesRequired)
            .map(NO::equals).orElse(false)) {
            return smallClaimHearing.toBuilder()
                .smallClaimUnavailableDate(null).build();
        }
        return smallClaimHearing;
    }

    Document getDraftDirections();

    RequestedCourt getRequestedCourt();

    HearingSupport getHearingSupport();

    FurtherInformation getFurtherInformation();

    WelshLanguageRequirements getWelshLanguageRequirements();

    StatementOfTruth getStatementOfTruth();

    FutureApplications getFutureApplications();
}
