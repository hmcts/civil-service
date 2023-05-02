package uk.gov.hmcts.reform.hearings.hearingnotice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendees {

    private String partyID;

    private String hearingSubChannel;
}
