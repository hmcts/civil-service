package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
public class SdoFastTrackTemplateFieldService {

    private static final String MINUTES = " minutes";
    private static final String OTHER = "Other";
    private static final String FAST_TRACK_ALLOCATION_BASE = "The claim is allocated to the Fast Track";
    private static final String FAST_TRACK_ALLOCATION_WITH_COMPLEXITY = " and is assigned to complexity %s";
    private static final String FAST_TRACK_ALLOCATION_NO_COMPLEXITY = " and is not assigned to a complexity band";
    private static final String FAST_TRACK_ALLOCATION_REASON = " because %s";

    public String getMethodTelephoneHearingLabel(CaseData caseData) {
        FastTrackMethodTelephoneHearing hearing = caseData.getFastTrackMethodTelephoneHearing();
        return hearing != null ? hearing.getLabel() : "";
    }

    public String getMethodVideoConferenceHearingLabel(CaseData caseData) {
        FastTrackMethodVideoConferenceHearing hearing = caseData.getFastTrackMethodVideoConferenceHearing();
        return hearing != null ? hearing.getLabel() : "";
    }

    public String getHearingTimeLabel(CaseData caseData) {
        FastTrackHearingTime hearingTime = caseData.getFastTrackHearingTime();

        if (Optional.ofNullable(hearingTime)
            .map(FastTrackHearingTime::getHearingDuration)
            .map(FastTrackHearingTimeEstimate::getLabel).isPresent()) {
            if (OTHER.equals(hearingTime.getHearingDuration().getLabel())) {
                StringBuilder otherLength = new StringBuilder();
                if (hearingTime.getOtherHours() != null) {
                    otherLength.append(hearingTime.getOtherHours().trim()).append(" hours ");
                }
                if (hearingTime.getOtherMinutes() != null) {
                    otherLength.append(hearingTime.getOtherMinutes().trim()).append(MINUTES);
                }
                return otherLength.toString();
            }
            return hearingTime.getHearingDuration().getLabel();
        }

        return "";
    }

    public String getAllocationSummary(CaseData caseData) {
        FastTrackAllocation allocation = caseData.getFastTrackAllocation();
        if (allocation == null) {
            return "";
        }

        String reasons = buildFastTrackAllocationReason(allocation);
        if (allocation.getAssignComplexityBand() == NO) {
            return FAST_TRACK_ALLOCATION_BASE + FAST_TRACK_ALLOCATION_NO_COMPLEXITY + reasons;
        } else if (allocation.getAssignComplexityBand() == YES && allocation.getBand() != null) {
            String band = String.format(
                FAST_TRACK_ALLOCATION_WITH_COMPLEXITY,
                allocation.getBand().getLabel().toLowerCase()
            );
            return FAST_TRACK_ALLOCATION_BASE + band + reasons;
        }

        return "";
    }

    private String buildFastTrackAllocationReason(FastTrackAllocation allocation) {
        if (allocation.getReasons() != null && !allocation.getReasons().isBlank()) {
            return String.format(FAST_TRACK_ALLOCATION_REASON, allocation.getReasons());
        }
        return "";
    }
}
