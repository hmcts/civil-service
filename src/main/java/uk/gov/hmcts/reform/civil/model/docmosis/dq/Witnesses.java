package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.dq.Witness;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Witnesses {

    private YesOrNo witnessesToAppear;
    private List<Witness> details;
}
