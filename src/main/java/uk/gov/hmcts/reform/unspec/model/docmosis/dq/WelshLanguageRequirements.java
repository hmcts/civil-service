package uk.gov.hmcts.reform.unspec.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;

@Data
@Builder
public class WelshLanguageRequirements {

    private final YesOrNo isPartyWelsh;
    private final String evidence;
    private final String court;
    private final String documents;
}
