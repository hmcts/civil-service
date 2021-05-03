package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.dq.Witness;

import java.util.List;

@Data
@Builder
public class Witnesses {

    private final YesOrNo witnessesToAppear;
    private final List<Witness> details;
}
