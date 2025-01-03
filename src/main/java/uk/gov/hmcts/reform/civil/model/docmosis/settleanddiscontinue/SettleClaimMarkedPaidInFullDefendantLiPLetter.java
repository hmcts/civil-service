package uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class SettleClaimMarkedPaidInFullDefendantLiPLetter implements MappableObject {

    private final String claimReferenceNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate letterIssueDate;
    private String letterIssueDateWelsh;
    private final String defendantLipName;
    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String postCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate dateOfEvent;
    private String dateOfEventWelsh;
}
