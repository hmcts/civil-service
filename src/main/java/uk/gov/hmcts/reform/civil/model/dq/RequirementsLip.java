package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class RequirementsLip {

    private final String name;
    private final List<SupportRequirements> requirements;
    private final String signLanguageRequired;
    private final String languageToBeInterpreted;
    private final String otherSupport;
}
