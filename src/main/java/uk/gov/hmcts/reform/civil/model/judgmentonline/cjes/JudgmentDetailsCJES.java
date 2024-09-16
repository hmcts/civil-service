package uk.gov.hmcts.reform.civil.model.judgmentonline.cjes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentDetailsCJES {

    private String serviceId;
    private String judgmentId;
    private LocalDateTime judgmentEventTimeStamp;
    private String courtEPIMsId;
    @JsonProperty("CCDCaseRef")
    private String ccdCaseRef;
    private String caseNumber;
    private Double judgmentAdminOrderTotal;
    private LocalDate judgmentAdminOrderDate;
    private String registrationType;
    private LocalDate cancellationDate;
    private JudgmentDefendantDetails defendant1;
    private JudgmentDefendantDetails defendant2;
}
