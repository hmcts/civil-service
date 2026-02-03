package uk.gov.hmcts.reform.civil.model.docmosis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CoverLetter implements MappableObject {

    private Party party;
}
