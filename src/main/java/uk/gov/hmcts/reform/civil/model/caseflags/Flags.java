package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(generate = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Flags {

    protected String partyName;
    protected String roleOnCase;
    protected List<Element<FlagDetail>> details;
}
