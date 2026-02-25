package uk.gov.hmcts.reform.civil.sampledata;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.hmc.model.hearing.Attendees;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel;
import uk.gov.hmcts.reform.hmc.model.hearing.IndividualDetailsModel;
import uk.gov.hmcts.reform.hmc.model.hearing.PartyDetailsModel;

import java.util.UUID;

import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.INTER;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.NA;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.TELCVP;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.VIDCVP;

@NoArgsConstructor
@Accessors(chain = true)
@Data
public class HearingIndividual {

    private String partyId;
    private String firstName;
    private String lastName;
    private HearingSubChannel hearingSubChannel;

    public PartyDetailsModel buildPartyDetails() {
        IndividualDetailsModel individualDetails = new IndividualDetailsModel();
        individualDetails.setFirstName(firstName);
        individualDetails.setLastName(lastName);
        PartyDetailsModel partyDetails = new PartyDetailsModel();
        partyDetails.setPartyID(partyId);
        partyDetails.setIndividualDetails(individualDetails);
        return partyDetails;
    }

    public Attendees buildAttendee() {
        return new Attendees()
                .setPartyID(partyId)
                .setHearingSubChannel(hearingSubChannel);
    }

    private static HearingIndividual attendingHearingBy(String firstName, String lastName, HearingSubChannel hearingSubChannel) {
        return new HearingIndividual()
                .setPartyId(UUID.randomUUID().toString())
                .setFirstName(firstName)
                .setLastName(lastName)
                .setHearingSubChannel(hearingSubChannel);
    }

    public static HearingIndividual attendingHearingInPerson(String firstName, String lastName) {
        return attendingHearingBy(firstName, lastName, INTER);
    }

    public static HearingIndividual attendingHearingByPhone(String firstName, String lastName) {
        return attendingHearingBy(firstName, lastName, TELCVP);
    }

    public static HearingIndividual attendingHearingByVideo(String firstName, String lastName) {
        return attendingHearingBy(firstName, lastName, VIDCVP);
    }

    public static HearingIndividual nonAttending(String firstName, String lastName) {
        return attendingHearingBy(firstName, lastName, NA);
    }
}
