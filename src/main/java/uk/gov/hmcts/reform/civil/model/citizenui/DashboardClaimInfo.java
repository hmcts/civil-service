package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate responseDeadline;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate paymentDate;
    private DashboardClaimStatus status;

    public long getNumberOfDays() {
        return Optional.of(responseDeadline).map(deadline -> ChronoUnit.DAYS.between(deadline, LocalDate.now()))
            .orElse(0L);
    }
}
