package uk.gov.hmcts.reform.civil.model.transferOnlineCase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TocTransferCaseReason {
    private String reasonForCaseTransferJudgeTxt;
}
