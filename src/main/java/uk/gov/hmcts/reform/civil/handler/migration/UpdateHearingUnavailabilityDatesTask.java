package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;

import java.util.List;

@Component
public class UpdateHearingUnavailabilityDatesTask extends UpdateUnavailableDatesTask {

    public UpdateHearingUnavailabilityDatesTask() {
        super(
            "UpdateHearingUnavailabilityDatesTask",
            "Update hearing unavailable dates via migration task",
            "This task restores missing hearing unavailable dates on the case"
        );
    }

    @Override
    protected List<Element<UnavailableDate>> getUnavailableDates(CaseData caseData, String partyType) {
        SmallClaimHearing hearing = isDefendant(partyType)
            ? getRespondentHearing(caseData)
            : getApplicantHearing(caseData);
        if (hearing == null && isDefendant(partyType)) {
            Hearing fastTrackHearing = getRespondentFastTrackHearing(caseData);
            return unavailableDatesOrEmpty(fastTrackHearing == null ? null : fastTrackHearing.getUnavailableDates());
        }
        return unavailableDatesOrEmpty(hearing == null ? null : hearing.getSmallClaimUnavailableDate());
    }

    @Override
    protected List<List<Element<UnavailableDate>>> getUnavailableDateCollections(
        CaseData caseData,
        String partyType
    ) {
        return List.of(
            getUnavailableDates(caseData, partyType),
            getHearingUnavailableDates(caseData, partyType),
            getUnavailableDatesForTab(caseData, partyType),
            getPartyUnavailableDates(caseData, partyType)
        );
    }

    private List<Element<UnavailableDate>> getPartyUnavailableDates(CaseData caseData, String partyType) {
        Party party = isDefendant(partyType)
            ? caseData.getRespondent1()
            : caseData.getApplicant1();
        return unavailableDatesOrEmpty(party == null ? null : party.getUnavailableDates());
    }

    private List<Element<UnavailableDate>> getHearingUnavailableDates(CaseData caseData, String partyType) {
        Hearing hearing;
        if (isDefendant(partyType)) {
            hearing = caseData.getRespondent1DQ() == null
                ? null
                : caseData.getRespondent1DQ().getRespondent1DQHearing();
        } else {
            hearing = caseData.getApplicant1DQ() == null
                ? null
                : caseData.getApplicant1DQ().getApplicant1DQHearing();
        }
        return unavailableDatesOrEmpty(hearing == null ? null : hearing.getUnavailableDates());
    }

    private List<Element<UnavailableDate>> getUnavailableDatesForTab(CaseData caseData, String partyType) {
        return unavailableDatesOrEmpty(
            isDefendant(partyType)
                ? caseData.getRespondent1UnavailableDatesForTab()
                : caseData.getApplicant1UnavailableDatesForTab()
        );
    }

    private SmallClaimHearing getRespondentHearing(CaseData caseData) {
        return caseData.getRespondent1DQ() == null
            ? null
            : caseData.getRespondent1DQ().getRespondent1DQHearingSmallClaim();
    }

    private SmallClaimHearing getApplicantHearing(CaseData caseData) {
        return caseData.getApplicant1DQ() == null
            ? null
            : caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing();
    }

    private Hearing getRespondentFastTrackHearing(CaseData caseData) {
        return caseData.getRespondent1DQ() == null
            ? null
            : caseData.getRespondent1DQ().getRespondent1DQHearingFastClaim();
    }
}
