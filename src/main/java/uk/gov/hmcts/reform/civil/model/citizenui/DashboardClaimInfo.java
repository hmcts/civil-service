package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.citizenui.DtoFieldFormat.DATE_FORMAT;
import static uk.gov.hmcts.reform.civil.model.citizenui.DtoFieldFormat.DATE_TIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardClaimInfo {

    private String claimId;
    private String claimNumber;
    private String claimantName;
    private String defendantName;
    private boolean ocmc;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal claimAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal admittedAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate responseDeadline;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime responseDeadlineTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate paymentDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime ccjRequestedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdDate;
    private DashboardClaimStatus status;
    private BigDecimal respondToAdmittedClaimOwingAmountPounds;

    @JsonGetter("numberOfDays")
    public long getNumberOfDays() {
        return Optional.ofNullable(responseDeadlineTime).map(
            time -> ChronoUnit.DAYS.between(LocalDateTime.now(), responseDeadlineTime)
        ).orElse(0L);
    }

    @JsonGetter("numberOfDaysOverdue")
    public long numberOfDaysOverdue() {
        return Optional.ofNullable(responseDeadline)
            .filter(deadline ->
                        deadline.isBefore(LocalDate.now()))
            .map(deadline ->
                     deadline.until(
                         LocalDate.now(),
                         ChronoUnit.DAYS
                     ))
            .orElse(0L);
    }
}
