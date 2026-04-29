package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;

import java.util.ArrayList;
import java.util.List;

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
        return duration != GAHearingDuration.OTHER ? duration.getDisplayedValue() : buildCustomEstimate(listForHearing);
    }

    private String buildCustomEstimate(GAJudgesHearingListGAspec listForHearing) {
        List<String> durationParts = new ArrayList<>();
        addDurationPart(durationParts, listForHearing.getJudicialTimeEstimateDays(), "day");
        addDurationPart(durationParts, listForHearing.getJudicialTimeEstimateHours(), "hour");
        addDurationPart(durationParts, listForHearing.getJudicialTimeEstimateMinutes(), "minute");
        return formatDurationParts(durationParts);
    }

    private void addDurationPart(List<String> durationParts, String rawValue, String unit) {
        int value = parseDurationPart(rawValue);
        if (value > 0) {
            durationParts.add(String.format("%d %s%s", value, unit, getPlural(value)));
        }
    }

    private int parseDurationPart(String rawValue) {
        return hasLength(rawValue) ? Integer.parseInt(rawValue) : 0;
    }

    private String formatDurationParts(List<String> durationParts) {
        if (durationParts.isEmpty()) {
            return "0 minutes";
        }

        if (durationParts.size() == 1) {
            return durationParts.getFirst();
        }

        int lastPartIndex = durationParts.size() - 1;
        return String.join(", ", durationParts.subList(0, lastPartIndex))
            + " and " + durationParts.get(lastPartIndex);
    }
}
