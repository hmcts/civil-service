package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Flags {

    private final String partyName;
    private final String roleOnCase;
    private final List<Element<FlagDetail>> details;
}
