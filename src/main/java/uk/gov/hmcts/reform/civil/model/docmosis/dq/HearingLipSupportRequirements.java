package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;

import java.util.List;

@Builder
@Data
public class HearingLipSupportRequirements {

    private final List<RequirementsLip> requirementsLip;

    @JsonIgnore
    public static HearingLipSupportRequirements toHearingSupportRequirements(HearingSupportLip hearingSupportLip){
          return hearingSupportLip!= null?
              HearingLipSupportRequirements.builder()
                  .requirementsLip(hearingSupportLip.getUnwrappedRequirementsLip())
                  .build()
              : HearingLipSupportRequirements.builder().build();

    }
}
