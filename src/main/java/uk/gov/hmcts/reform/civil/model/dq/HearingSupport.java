package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class HearingSupport {

    // CIV-5557 to be removed
    private List<SupportRequirements> requirements;
    private String signLanguageRequired;
    private String languageToBeInterpreted;
    private String otherSupport;
    private YesOrNo supportRequirements;
    private String supportRequirementsAdditional;
}
