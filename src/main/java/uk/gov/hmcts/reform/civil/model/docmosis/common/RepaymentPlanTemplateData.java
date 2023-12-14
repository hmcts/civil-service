package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class RepaymentPlanTemplateData {

    private String paymentFrequencyDisplay;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal paymentAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate firstRepaymentDate;
}
