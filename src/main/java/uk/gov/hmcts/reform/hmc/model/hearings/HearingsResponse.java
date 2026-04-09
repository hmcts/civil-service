package uk.gov.hmcts.reform.hmc.model.hearings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class HearingsResponse {

    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearing> caseHearings;

}
