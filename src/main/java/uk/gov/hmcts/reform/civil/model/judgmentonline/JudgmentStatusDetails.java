package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentStatusDetails {

    private JudgmentStatusType judgmentStatusTypes;
    private LocalDateTime lastUpdatedDate;
    private String joRtlState;
}
