package uk.gov.hmcts.reform.hmc.model.hearing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Attendees {

    private String partyID;

    private HearingSubChannel hearingSubChannel;
}
