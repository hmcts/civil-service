package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HearingLipSupportRequirements {

    private String name;
    private List<DisabilityRequirement> requirements;
    private String otherSupport;

    @JsonIgnore
    public static HearingLipSupportRequirements toHearingSupportRequirements(RequirementsLip hearingSupportLip) {
        return hearingSupportLip != null ? new HearingLipSupportRequirements()
            .setOtherSupport(hearingSupportLip.getOtherSupport())
            .setName(hearingSupportLip.getName())
            .setRequirements(Optional.ofNullable(hearingSupportLip.getRequirements()).map(Collection::stream).map(
                requirements -> requirements.map(requirement -> DisabilityRequirement.toDisabilityRequirements(
                    requirement,
                    hearingSupportLip.getOtherSupport()
                )).toList()).orElse(Collections.emptyList()))
            : new HearingLipSupportRequirements();

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
