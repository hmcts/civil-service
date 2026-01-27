package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Witnesses {

    private YesOrNo witnessesToAppear;
    private List<Element<Witness>> details;

    public Witnesses copy() {
        return new Witnesses()
            .setWitnessesToAppear(witnessesToAppear)
            .setDetails(details);
    }
}
