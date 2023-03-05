package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TempHearingValuesModel {

    private final String publicCaseName;
}
