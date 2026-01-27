package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SdoCoverLetter implements MappableObject {

    private Party party;
    private String claimReferenceNumber;
}
