package uk.gov.hmcts.reform.unspec.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.dq.Language;

@Data
@Builder
public class WelshLanguageRequirements {

    private final Language evidence;
    private final Language court;
    private final Language documents;
}
