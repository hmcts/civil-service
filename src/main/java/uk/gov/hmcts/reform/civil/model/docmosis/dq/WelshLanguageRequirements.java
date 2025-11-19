package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WelshLanguageRequirements {

    private String evidence;
    private String court;
    private String documents;
}
