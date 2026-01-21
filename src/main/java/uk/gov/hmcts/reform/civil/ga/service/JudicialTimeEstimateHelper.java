package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;

import static org.springframework.util.StringUtils.hasLength;

@Service
@RequiredArgsConstructor
public class JudicialTimeEstimateHelper {

    private static String getPlural(int value) {
        return value > 1 ? "s" : "";
    }

    public String getEstimatedHearingLength(GeneralApplicationCaseData caseData) {
        GAJudgesHearingListGAspec listForHearing = caseData.getJudicialListForHearing();
        GAHearingDuration duration = listForHearing.getJudicialTimeEstimate();
        if (duration != GAHearingDuration.OTHER) {
            return duration.getDisplayedValue();
        }
        StringBuilder hearingLengthText = new StringBuilder();
        int days = hasLength(listForHearing.getJudicialTimeEstimateDays())
            ? Integer.valueOf(listForHearing.getJudicialTimeEstimateDays()) : 0;
        int hours = hasLength(listForHearing.getJudicialTimeEstimateHours())
            ? Integer.valueOf(listForHearing.getJudicialTimeEstimateHours()) : 0;
        int minutes = hasLength(listForHearing.getJudicialTimeEstimateMinutes())
            ? Integer.valueOf(listForHearing.getJudicialTimeEstimateMinutes()) : 0;
        if (days > 0) {
            hearingLengthText.append(String.format("%d day%s", days, getPlural(days)));
            if (hours > 0) {
                if (minutes > 0) {
                    hearingLengthText.append(String.format(
                        ", %d hour%s and %d minute%s", hours, getPlural(hours),
                        minutes, getPlural(minutes)
                    ));
                } else {
                    hearingLengthText.append(String.format(" and %d hour%s", hours, getPlural(hours)));
                }
            } else if (minutes > 0) {
                hearingLengthText.append(String.format(" and %d minute%s", minutes, getPlural(minutes)));
            }
        } else if (hours > 0) {
            hearingLengthText.append(String.format("%d hour%s", hours, getPlural(hours)));
            if (minutes > 0) {
                hearingLengthText.append(String.format(" and %d minute%s", minutes, getPlural(minutes)));
            }
        } else {
            hearingLengthText.append(String.format("%d minute%s", minutes, getPlural(minutes)));
        }
        return hearingLengthText.toString();
    }
}
