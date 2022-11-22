package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.common.DynamicList;
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
        return nonNull(hearingTime) && nonNull(hearingTime.getHearingTimeEstimate())
            ? hearingTime.getHearingTimeEstimate().getLabel() : null;
    }
}
