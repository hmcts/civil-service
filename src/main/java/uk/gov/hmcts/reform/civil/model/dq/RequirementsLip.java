package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class RequirementsLip {

    private String name;
    private List<SupportRequirements> requirements;
    private String signLanguageRequired;
    private String languageToBeInterpreted;
    private String otherSupport;
}
