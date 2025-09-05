package uk.gov.hmcts.reform.hmc.model.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class HearingGetResponse {

    private HearingRequestDetails requestDetails;

    private HearingDetails hearingDetails;

    private CaseDetailsHearing caseDetails;

    private List<PartyDetailsModel> partyDetails;

    private HearingResponse hearingResponse;

}
