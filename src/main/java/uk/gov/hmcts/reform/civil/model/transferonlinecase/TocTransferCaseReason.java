package uk.gov.hmcts.reform.civil.model.transferonlinecase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TocTransferCaseReason {

    private String reasonForCaseTransferJudgeTxt;
}
