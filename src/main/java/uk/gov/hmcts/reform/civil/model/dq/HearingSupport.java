package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;

import java.util.List;

@Data
@Builder
public class HearingSupport {

    private final List<SupportRequirements> requirements;
    private final String signLanguageRequired;
    private final String languageToBeInterpreted;
    private final String otherSupport;
}
