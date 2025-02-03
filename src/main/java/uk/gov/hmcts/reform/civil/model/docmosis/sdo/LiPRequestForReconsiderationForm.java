package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class LiPRequestForReconsiderationForm implements MappableObject {

    private final String caseNumber;

    private final LocalDate currentDate;
    private final String countyCourt;
    private final String partyName;
    private final Address partyAddress;
    private final String requestReason;
}
