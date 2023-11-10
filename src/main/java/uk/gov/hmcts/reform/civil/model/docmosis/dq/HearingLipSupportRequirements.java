package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Builder
@Data
public class HearingLipSupportRequirements {

    private final String name;
    private final List<DisabilityRequirement> requirements;
    private final String otherSupport;

    @JsonIgnore
    public static HearingLipSupportRequirements toHearingSupportRequirements(RequirementsLip hearingSupportLip) {
        return hearingSupportLip != null ? HearingLipSupportRequirements.builder()
            .otherSupport(hearingSupportLip.getOtherSupport())
            .name(hearingSupportLip.getName())
            .requirements(Optional.ofNullable(hearingSupportLip.getRequirements()).map(Collection::stream).map(
                requirements -> requirements.map(requirement -> DisabilityRequirement.toDisabilityRequirements(
                    requirement,
                    hearingSupportLip.getOtherSupport()
                )).toList()).orElse(Collections.emptyList()))
            .build()
            : HearingLipSupportRequirements.builder().build();

    }

    @JsonIgnore
    public static List<HearingLipSupportRequirements> toHearingSupportRequirementsList(Optional<HearingSupportLip> hearingSupportLip) {
        return hearingSupportLip.map(HearingSupportLip::getUnwrappedRequirementsLip)
            .map(Collection::stream)
            .map(items -> items.map(item -> toHearingSupportRequirements(item))
                .toList())
            .orElse(Collections.emptyList());
    }
}
