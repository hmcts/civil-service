package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;

import static java.util.Objects.nonNull;

public class DocumentUtils {

    private DocumentUtils() {
        //NO-OP
    }

    public static String getDynamicListValueLabel(DynamicList dynamicList) {
        return nonNull(dynamicList) && nonNull(dynamicList.getValue()) ? dynamicList.getValue().getLabel() : null;
    }

    public static String getHearingTimeEstimateLabel(TrialHearingTimeDJ hearingTime) {
        String hearingTimeEstimateLabel = nonNull(hearingTime) && nonNull(hearingTime.getHearingTimeEstimate())
            ? hearingTime.getHearingTimeEstimate().getLabel() : null;

        if (hearingTimeEstimateLabel != null && hearingTimeEstimateLabel.equals("Other")) {
            StringBuilder otherLength = new StringBuilder();
            if (hearingTime.getOtherHours() != null) {
                otherLength.append(hearingTime.getOtherHours().trim() +
                                       " hours ");
            }
            if (hearingTime.getOtherMinutes() != null) {
                otherLength.append(hearingTime.getOtherMinutes().trim() + " minutes");
            }
            return otherLength.toString();
        }
        return hearingTimeEstimateLabel;
    }

    public static String getDisposalHearingTimeEstimateDJ(DisposalHearingFinalDisposalHearingTimeDJ hearingTime) {
        String hearingTimeEstimateLabel = nonNull(hearingTime) && nonNull(hearingTime.getTime())
            ? hearingTime.getTime().getLabel() : null;

        if (hearingTimeEstimateLabel != null && hearingTimeEstimateLabel.equals("Other")) {
            StringBuilder otherLength = new StringBuilder();
            if (hearingTime.getOtherHours() != null && Integer.parseInt(hearingTime.getOtherHours()) != 0) {
                String hourString = Integer.parseInt(hearingTime.getOtherHours()) == 1
                    ? " hour" : " hours";
                otherLength.append(hearingTime.getOtherHours().trim() + hourString);
            }
            if (hearingTime.getOtherMinutes() != null && Integer.parseInt(hearingTime.getOtherMinutes()) != 0) {
                String minuteString = Integer.parseInt(hearingTime.getOtherMinutes()) == 1
                    ? " minute" : " minutes";
                String spaceBeforeMinute = otherLength.toString().contains("hour") ? " " : "";
                otherLength.append(spaceBeforeMinute
                                       + hearingTime.getOtherMinutes().trim()
                                       + minuteString);
            }
            return otherLength.toString();
        }
        return hearingTimeEstimateLabel;
    }
}
