package uk.gov.hmcts.reform.civil.service.bulkclaims;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseWorkerSearchCaseParams {

    private String authorisation;
    private String userId;
    private Map<String, String> searchCriteria;
}
