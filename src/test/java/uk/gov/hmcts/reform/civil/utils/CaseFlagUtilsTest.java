package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CaseFlagUtilsTest {

    @Nested
    class CreateFlags {

        @Test
        void shouldCreateFlags() {
            Flags expected = Flags.builder().partyName("partyName").roleOnCase("roleOnCase").details(List.of()).build();
            Flags actual = CaseFlagUtils.createFlags("partyName", "roleOnCase");
            assertEquals(expected, actual);
        }
    }

    @Nested
    class UpdateParty {

        @Test
        void shouldUpdatePartyWithFlagsMeta() {
            Party party = PartyBuilder.builder().individual().build();
            Flags flags = Flags.builder().partyName("Mr. John Rambo").roleOnCase("applicant").details(List.of()).build();
            Party expected = party.toBuilder().flags(flags).build();

            Party actual = CaseFlagUtils.updateParty("applicant", party);

            assertEquals(expected, actual);
        }

        @Test
        void shouldNotUpdatePartyFlagsIfFlagsExist() {
            Party existingParty = PartyBuilder.builder().individual().build()
                .toBuilder()
                .flags(Flags.builder().partyName("Mr. John Rambo").roleOnCase("applicant").details(List.of()).build())
                .build();

            Party actual = CaseFlagUtils.updateParty("updatedField", existingParty);

            assertEquals(existingParty, actual);
        }

        @Test
        void shouldReturnNullWhenPartyIsNull() {
            Party actual = CaseFlagUtils.updateParty("applicant", null);
            assertNull(actual);
        }
    }

    @Nested
    class UpdateLitFriend {

        @Test
        void shouldUpdateLitigationFriendWithFlagsMeta() {
            LitigationFriend litFriend = LitigationFriend.builder().firstName("John").lastName("Rambo").build();
            Flags flags = Flags.builder().partyName("John Rambo").roleOnCase("applicant").details(List.of()).build();
            LitigationFriend expected = litFriend.toBuilder().flags(flags).build();

            LitigationFriend actual = CaseFlagUtils.updateLitFriend("applicant", litFriend);

            assertEquals(expected, actual);
        }

        @Test
        void shouldNotUpdateLitigationFriendFlagsIfFlagsExist() {
            LitigationFriend existingLitFriend = LitigationFriend.builder().firstName("John").lastName("Rambo").build()
                .toBuilder()
                .flags(Flags.builder().partyName("John Rambo").roleOnCase("applicant").details(List.of()).build())
                .build();

            LitigationFriend actual = CaseFlagUtils.updateLitFriend("updatedField", existingLitFriend);

            assertEquals(existingLitFriend, actual);
        }

        @Test
        void shouldReturnNullWhenLitigationFriendIsNull() {
            LitigationFriend actual = CaseFlagUtils.updateLitFriend("applicant", null);
            assertNull(actual);
        }
    }

}
