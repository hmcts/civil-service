package uk.gov.hmcts.reform.civil.model.docmosis.pip;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PiPLetter implements MappableObject {

    private final Party defendant;
    private final String claimantName;
    private final String respondToClaimUrl;
    private final BigDecimal totalAmountOfClaim;
    private final String claimReferenceNumber;
    private final String pin;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate responseDeadline;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate issueDate;

}
