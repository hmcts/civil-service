package uk.gov.hmcts.reform.civil.sampledata;

import lombok.Builder;
import uk.gov.hmcts.reform.hmc.model.hearing.Attendees;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel;
import uk.gov.hmcts.reform.hmc.model.hearing.IndividualDetailsModel;
import uk.gov.hmcts.reform.hmc.model.hearing.PartyDetailsModel;

import java.util.UUID;

import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.INTER;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.NA;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.TELCVP;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.VIDCVP;

@Builder
public class HearingIndividual {

    private String partyId;
    private String firstName;
    private String lastName;
    private HearingSubChannel hearingSubChannel;

    public PartyDetailsModel buildPartyDetails() {
        return PartyDetailsModel.builder()
                .partyID(partyId)
                .individualDetails(IndividualDetailsModel.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .build())
                .build();
    }

    public Attendees buildAttendee() {
        return Attendees.builder()
                .partyID(partyId)
                .hearingSubChannel(hearingSubChannel)
                .build();
    }

    private static HearingIndividual attendingHearingBy(String firstName, String lastName, HearingSubChannel hearingSubChannel) {
        return HearingIndividual.builder()
                .partyId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .hearingSubChannel(hearingSubChannel)
                .build();
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
