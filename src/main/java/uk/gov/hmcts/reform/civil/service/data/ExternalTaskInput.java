package uk.gov.hmcts.reform.civil.service.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalTaskInput {

    String caseId;
    CaseEvent caseEvent;
    String generalAppParentCaseLink;
    Boolean triggeredViaScheduler;
    String generalApplicationCaseId;
}
