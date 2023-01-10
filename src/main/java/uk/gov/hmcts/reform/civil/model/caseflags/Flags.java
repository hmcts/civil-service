package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Setter
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Flags {

    private String partyName;
    private String roleOnCase;
    private final List<Element<FlagDetail>> details;
}
