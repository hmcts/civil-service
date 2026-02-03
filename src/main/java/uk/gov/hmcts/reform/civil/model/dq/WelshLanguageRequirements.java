package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dq.Language;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WelshLanguageRequirements {

    private Language evidence;
    private Language court;
    private Language documents;
}
