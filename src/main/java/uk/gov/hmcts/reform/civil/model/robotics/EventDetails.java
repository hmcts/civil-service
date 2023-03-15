package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDetails {

    private String miscText;
    private String responseIntention;
    private String acknowledgeService;
    private String agreedExtensionDate;
    private Boolean stayClaim;
    private String preferredCourtCode;
    private String preferredCourtName;
    private BigDecimal amountOfJudgment;
    private BigDecimal amountOfCosts;
    private BigDecimal amountPaidBeforeJudgment;
    private Boolean isJudgmentForthwith;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime paymentInFullDate;
    private BigDecimal installmentAmount;
    private String installmentPeriod;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate firstInstallmentDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime dateOfJudgment;
    private Boolean jointJudgment;
    private Boolean judgmentToBeRegistered;

}
