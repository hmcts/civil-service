package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;

import java.util.List;

@Component
public class UpdateSmallClaimHearingUnavailabilityDatesTask extends UpdateUnavailableDatesTask {

    private static final String DEFENDANT = "defendant";

    @Override
    protected String getEventSummary() {
        return "Update small claim hearing unavailable dates via migration task";
    }

    @Override
    protected String getTaskName() {
        return "UpdateSmallClaimHearingUnavailabilityDatesTask";
    }

    @Override
    protected String getEventDescription() {
        return "This task restores missing small claim hearing unavailable dates on the case";
    }

    @Override
    protected List<Element<UnavailableDate>> getUnavailableDates(CaseData caseData, String partyType) {
        SmallClaimHearing hearing = DEFENDANT.equals(partyType)
            ? getRespondentHearing(caseData)
            : getApplicantHearing(caseData);
        if (hearing == null || hearing.getSmallClaimUnavailableDate() == null
            || hearing.getSmallClaimUnavailableDate().isEmpty()) {
            throw new IllegalStateException("Small claim hearing unavailable dates must not be null or empty");
        }
        return hearing.getSmallClaimUnavailableDate();
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
}
