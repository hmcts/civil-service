package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LiPRequestForReconsiderationForm implements MappableObject {

    private String caseNumber;

    private LocalDate currentDate;
    private String countyCourt;
    private String partyName;
    private Address partyAddress;
    private String requestReason;
}
