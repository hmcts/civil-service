package uk.gov.hmcts.reform.hmc.model.hearing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HearingGetResponse {

    private HearingRequestDetails requestDetails;

    private HearingDetails hearingDetails;

    private CaseDetailsHearing caseDetails;

    private List<PartyDetailsModel> partyDetails;

    private HearingResponse hearingResponse;

}
