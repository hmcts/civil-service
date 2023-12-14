package uk.gov.hmcts.reform.civil.service.bulkclaims;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseWorkerSearchCaseParams {

    private String authorisation;
    private String userId;
    private Map<String, String> searchCriteria;
}
