package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class Flags {

    private final String partyName;
    private final String roleOnCase;
    private final List<Element<FlagDetail>> details;
}
