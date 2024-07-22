package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.Language;

@Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@Builder(toBuilder = true)
public class WelshLanguageRequirements {

    private Language evidence;
    private Language court;
    private Language documents;
}
