package uk.gov.hmcts.reform.hearings.hearingnotice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.PartyDetailsModel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingGetResponse {

    @NonNull
    private HearingRequestDetails requestDetails;

    @NonNull
    private HearingDetails hearingDetails;

    @NonNull
    private CaseDetailsHearing caseDetails;

    @NonNull
    private List<PartyDetailsModel> partyDetails;

    @NonNull
    private HearingResponse hearingResponse;

}
