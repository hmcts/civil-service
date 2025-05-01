package uk.gov.hmcts.reform.civil.model.docmosis.manualdetermination;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ClaimantLipManualDeterminationForm implements MappableObject {

    private final String referenceNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate claimIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy 'at' HH:mm a")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime claimantResponseSubmitDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal defendantAdmittedAmount;
    private final PaymentType repaymentType;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal regularPaymentAmount;
    private String repaymentFrequency;
    private final String claimantRequestRepaymentBy;
    private final RespondentResponseTypeSpec claimResponseType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate firstRepaymentDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate lastRepaymentDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate paymentSetDateForDefendant;
}
