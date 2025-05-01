package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Flags {

    private String partyName;
    private String roleOnCase;
    private List<Element<FlagDetail>> details;
}
