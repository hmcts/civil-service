package uk.gov.hmcts.reform.hmc.model.hearings;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class HearingsResponse {

    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearing> caseHearings;

}
