package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RequirementsLip {

    private String name;
    private List<SupportRequirements> requirements;
    private String signLanguageRequired;
    private String languageToBeInterpreted;
    private String otherSupport;
}
