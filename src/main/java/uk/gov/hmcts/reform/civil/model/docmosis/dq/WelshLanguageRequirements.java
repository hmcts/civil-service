package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WelshLanguageRequirements {

    private final String evidence;
    private final String court;
    private final String documents;
}
