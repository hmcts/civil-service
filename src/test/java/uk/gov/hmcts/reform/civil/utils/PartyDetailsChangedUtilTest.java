package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyDetailsChange;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PartyDetailsChangedUtilTest {

    @InjectMocks
    private PartyDetailsChangedUtil partyDetailsChangedUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class BuildChangesEventTests {

        @Test
        public void testBuildChangesEvent_NoChanges() {
            CaseData current = CaseData.builder().applicant1(PartyBuilder.builder().individual().build()).build();
            CaseData updated = CaseData.builder().applicant1(PartyBuilder.builder().individual().build()).build();

            assertNull(partyDetailsChangedUtil.buildChangesEvent(current, updated));
        }

        @Test
        public void testBuildChangesEvent_Applicant1Changes() {
            CaseData current = CaseData.builder().applicant1(
                PartyBuilder.builder().company().build().toBuilder().companyName("Company One").build()
            ).build();
            CaseData updated = CaseData.builder().applicant1(
                PartyBuilder.builder().company().build().toBuilder().companyName("Company Two").build()
            ).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Applicant 1 Details Changed")
                .description("Name: From 'Company One' to 'Company Two'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2Changes() {
            CaseData current = CaseData.builder().applicant2(
                PartyBuilder.builder().individual().build().toBuilder().individualFirstName("John").individualLastName("Doe").build()
            ).build();

            CaseData updated = CaseData.builder().applicant2(
                PartyBuilder.builder().individual().build().toBuilder().individualFirstName("Jane").individualLastName("Smith").build()
            ).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Applicant 2 Details Changed")
                .description("Name: From 'John Doe' to 'Jane Smith'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1Changes() {
            CaseData current = CaseData.builder().respondent1(
                PartyBuilder.builder().individual().build().toBuilder().individualFirstName("John").individualLastName("Doe").build()
            ).build();

            CaseData updated = CaseData.builder().respondent1(
                PartyBuilder.builder().individual().build().toBuilder().individualFirstName("Jane").individualLastName("Smith").build()
            ).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Respondent 1 Details Changed")
                .description("Name: From 'John Doe' to 'Jane Smith'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent2Changes() {
            CaseData current = CaseData.builder().respondent2(
                PartyBuilder.builder().individual().build().toBuilder().individualFirstName("John").individualLastName("Doe").build()
            ).build();

            CaseData updated = CaseData.builder().respondent2(
                PartyBuilder.builder().individual().build().toBuilder().individualFirstName("Jane").individualLastName("Smith").build()
            ).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Respondent 2 Details Changed")
                .description("Name: From 'John Doe' to 'Jane Smith'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant1LitigationFriendChanges() {
            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("Jane")
                .lastName("Smith")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            CaseData caseDataCurrent = CaseData.builder().applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder().applicant1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Applicant 1 Litigation Friend Details Changed")
                .description("Name: From 'John Doe' to 'Jane Smith'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant1LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                Address.builder()
                    .addressLine1("Litigant Street")
                    .postTown("Litigant City")
                    .postCode("Litigant Postcode")
                    .country("Litigant Country")
                    .build()).build();

            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.YES)
                .build();

            CaseData caseDataCurrent = CaseData.builder()
                .applicant1(litigant)
                .applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .applicant1(litigant)
                .applicant1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Applicant 1 Litigation Friend Details Changed")
                .description("Address: From '123 Main St, City, Country' to 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant1LitigationFriendAddressChanges_toAddressNotSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                Address.builder()
                    .addressLine1("Litigant Street")
                    .postTown("Litigant City")
                    .postCode("Litigant Postcode")
                    .country("Litigant Country")
                    .build()).build();

            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.YES)
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            CaseData caseDataCurrent = CaseData.builder()
                .applicant1(litigant)
                .applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .applicant1(litigant)
                .applicant1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Applicant 1 Litigation Friend Details Changed")
                .description("Address: From 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country' to '123 Main St, City, Country'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2LitigationFriendChanges() {
            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("Jane")
                .lastName("Smith")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            CaseData caseDataCurrent = CaseData.builder().applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder().applicant2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Applicant 2 Litigation Friend Details Changed")
                .description("Name: From 'John Doe' to 'Jane Smith'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                Address.builder()
                    .addressLine1("Litigant Street")
                    .postTown("Litigant City")
                    .postCode("Litigant Postcode")
                    .country("Litigant Country")
                    .build()).build();

            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.YES)
                .build();

            CaseData caseDataCurrent = CaseData.builder()
                .applicant2(litigant)
                .applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .applicant2(litigant)
                .applicant2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Applicant 2 Litigation Friend Details Changed")
                .description("Address: From '123 Main St, City, Country' to 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2LitigationFriendAddressChanges_toAddressNotSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                Address.builder()
                    .addressLine1("Litigant Street")
                    .postTown("Litigant City")
                    .postCode("Litigant Postcode")
                    .country("Litigant Country")
                    .build()).build();

            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.YES)
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            CaseData caseDataCurrent = CaseData.builder()
                .applicant2(litigant)
                .applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .applicant2(litigant)
                .applicant2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Applicant 2 Litigation Friend Details Changed")
                .description("Address: From 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country' to '123 Main St, City, Country'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1LitigationFriendChanges() {
            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("Jane")
                .lastName("Smith")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            CaseData caseDataCurrent = CaseData.builder().respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder().respondent1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Respondent 1 Litigation Friend Details Changed")
                .description("Name: From 'John Doe' to 'Jane Smith'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                Address.builder()
                    .addressLine1("Litigant Street")
                    .postTown("Litigant City")
                    .postCode("Litigant Postcode")
                    .country("Litigant Country")
                    .build()).build();

            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .primaryAddress(
                    Address.builder()
                        .addressLine1("123 Main St")
                        .postTown("City")
                        .postCode("YO1 ERP")
                        .country("Country")
                        .build())
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.YES)
                .build();

            CaseData caseDataCurrent = CaseData.builder()
                .respondent1(litigant)
                .respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .respondent1(litigant)
                .respondent1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Respondent 1 Litigation Friend Details Changed")
                .description("Address: From '123 Main St, City, YO1 ERP, Country' to 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1LitigationFriendAddressChanges_toAddressNotSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                Address.builder()
                    .addressLine1("Litigant Street")
                    .postTown("Litigant City")
                    .postCode("Litigant Postcode")
                    .country("Litigant Country")
                    .build()).build();

            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.YES)
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            CaseData caseDataCurrent = CaseData.builder()
                .respondent1(litigant)
                .respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .respondent1(litigant)
                .respondent1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Respondent 1 Litigation Friend Details Changed")
                .description("Address: From 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country' to '123 Main St, City, Country'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent2LitigationFriendChanges() {
            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .hasSameAddressAsLitigant(YesOrNo.NO)
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("Jane")
                .lastName("Smith")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            CaseData caseDataCurrent = CaseData.builder().respondent2LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder().respondent2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
                .summary("Respondent 2 Litigation Friend Details Changed")
                .description("Name: From 'John Doe' to 'Jane Smith'.")
                .build();

            assertEquals(expectedEvent, actualEvent);
        }
    }

    @Test
    public void testBuildChangesEvent_Respondent2LitigationFriendAddressChanges_toAddressSameAsLitigant() {
        Party litigant = Party.builder().primaryAddress(
            Address.builder()
                .addressLine1("Litigant Street")
                .postTown("Litigant City")
                .postCode("Litigant Postcode")
                .country("Litigant Country")
                .build()).build();

        LitigationFriend current = LitigationFriend.builder()
            .firstName("John")
            .lastName("Doe")
            .hasSameAddressAsLitigant(YesOrNo.NO)
            .primaryAddress(
                Address.builder()
                    .addressLine1("123 Main St")
                    .postTown("City")
                    .postCode("YO1 ERP")
                    .country("Country")
                    .build())
            .build();

        LitigationFriend updated = LitigationFriend.builder()
            .firstName("John")
            .lastName("Doe")
            .hasSameAddressAsLitigant(YesOrNo.YES)
            .build();

        CaseData caseDataCurrent = CaseData.builder()
            .respondent2(litigant)
            .respondent2LitigationFriend(current).build();
        CaseData caseDataUpdated = CaseData.builder()
            .respondent2(litigant)
            .respondent2LitigationFriend(updated).build();

        ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

        ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
            .summary("Respondent 2 Litigation Friend Details Changed")
            .description("Address: From '123 Main St, City, YO1 ERP, Country' to 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country'.")
            .build();

        assertEquals(expectedEvent, actualEvent);
    }

    @Test
    public void testBuildChangesEvent_Respondent2LitigationFriendAddressChanges_toAddressNotSameAsLitigant() {
        Party litigant = Party.builder().primaryAddress(
            Address.builder()
                .addressLine1("Litigant Street")
                .postTown("Litigant City")
                .postCode("Litigant Postcode")
                .country("Litigant Country")
                .build()).build();

        LitigationFriend current = LitigationFriend.builder()
            .firstName("John")
            .lastName("Doe")
            .hasSameAddressAsLitigant(YesOrNo.YES)
            .build();

        LitigationFriend updated = LitigationFriend.builder()
            .firstName("John")
            .lastName("Doe")
            .hasSameAddressAsLitigant(YesOrNo.NO)
            .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
            .build();

        CaseData caseDataCurrent = CaseData.builder()
            .respondent2(litigant)
            .respondent2LitigationFriend(current).build();
        CaseData caseDataUpdated = CaseData.builder()
            .respondent2(litigant)
            .respondent2LitigationFriend(updated).build();

        ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

        ContactDetailsUpdatedEvent expectedEvent = ContactDetailsUpdatedEvent.builder()
            .summary("Respondent 2 Litigation Friend Details Changed")
            .description("Address: From 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country' to '123 Main St, City, Country'.")
            .build();

        assertEquals(expectedEvent, actualEvent);
    }

    @Nested
    class GetChangesPartyTests {

        @Test
        public void testGetChangesParty_NoChanges() {
            Party current = PartyBuilder.builder().individual().build();
            Party updated = PartyBuilder.builder().individual().build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            assertTrue(changes.isEmpty());
        }

        @Test
        public void testGetChangesParty_PartyNameChange_Individual() {
            Party current = PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("Jane")
                .individualLastName("Carver")
                .build();

            Party updated = PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("Jane")
                .individualLastName("Wilson")
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedNameChange = PartyDetailsChange.builder()
                .fieldName("Name")
                .previousValue("Jane Carver")
                .updatedValue("Jane Wilson")
                .build();

            assertEquals(List.of(expectedNameChange), changes);
        }

        @Test
        public void testGetChangesParty_PartyNameChange_SoleTrader() {
            Party current = PartyBuilder.builder().soleTrader().build().toBuilder()
                .soleTraderFirstName("Jane")
                .soleTraderLastName("Carver")
                .build();

            Party updated = PartyBuilder.builder().soleTrader().build().toBuilder()
                .soleTraderFirstName("Jane")
                .soleTraderLastName("Wilson")
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedNameChange = PartyDetailsChange.builder()
                .fieldName("Name")
                .previousValue("Jane Carver")
                .updatedValue("Jane Wilson")
                .build();

            assertEquals(List.of(expectedNameChange), changes);
        }

        @Test
        public void testGetChangesParty_PartyNameChange_Company() {
            Party current = PartyBuilder.builder().company().build().toBuilder()
                .companyName("Company One")
                .build();

            Party updated = PartyBuilder.builder().company().build().toBuilder()
                .companyName("Company Two")
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedNameChange = PartyDetailsChange.builder()
                .fieldName("Name")
                .previousValue("Company One")
                .updatedValue("Company Two")
                .build();

            assertEquals(List.of(expectedNameChange), changes);
        }

        @Test
        public void testGetChangesParty_PartyNameChange_Organisation() {
            Party current = PartyBuilder.builder().organisation().build().toBuilder()
                .organisationName("Organisation One")
                .build();

            Party updated = PartyBuilder.builder().organisation().build().toBuilder()
                .organisationName("Organisation Two")
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedNameChange = PartyDetailsChange.builder()
                .fieldName("Name")
                .previousValue("Organisation One")
                .updatedValue("Organisation Two")
                .build();

            assertEquals(List.of(expectedNameChange), changes);
        }

        @Test
        public void testGetChangesParty_AddressChange() {
            Party current = PartyBuilder.builder().individual().build().toBuilder()
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            Party updated = PartyBuilder.builder().individual().build().toBuilder()
                .primaryAddress(Address.builder().addressLine1("999 Main St").postTown("City").country("Country").build())
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedAddressChange = PartyDetailsChange.builder()
                .fieldName("Address")
                .previousValue("123 Main St, City, Country")
                .updatedValue("999 Main St, City, Country")
                .build();

            assertEquals(List.of(expectedAddressChange), changes);
        }

        @Test
        public void testGetChangesParty_MultipleChanges() {
            Party current = PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("John")
                .individualLastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            Party updated = PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("Jane")
                .individualLastName("Smith")
                .primaryAddress(Address.builder().addressLine1("999 Elm St").postTown("Town").country("Country").build())
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedNameChange = PartyDetailsChange.builder()
                .fieldName("Name")
                .previousValue("John Doe")
                .updatedValue("Jane Smith")
                .build();

            PartyDetailsChange expectedAddressChange = PartyDetailsChange.builder()
                .fieldName("Address")
                .previousValue("123 Main St, City, Country")
                .updatedValue("999 Elm St, Town, Country")
                .build();

            assertEquals(List.of(expectedNameChange, expectedAddressChange), changes);
        }
    }

    @Nested
    class GetChangesLitigationFriendTests {

        @Test
        public void testGetChangesLitigationFriend_NoChanges() {
            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);
            assertTrue(changes.isEmpty());
        }

        @Test
        public void testGetChangesLitigationFriend_NameChange() {
            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("Jane")
                .lastName("Smith")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedChange = PartyDetailsChange.builder()
                .fieldName("Name")
                .previousValue("John Doe")
                .updatedValue("Jane Smith")
                .build();

            assertEquals(List.of(expectedChange), changes);
        }

        @Test
        public void testGetChangesLitigationFriend_AddressChange() {
            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("999 Elm St").postTown("Town").country("Country").build())
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedAddressChange = PartyDetailsChange.builder()
                .fieldName("Address")
                .previousValue("123 Main St, City, Country")
                .updatedValue("999 Elm St, Town, Country")
                .build();

            assertEquals(List.of(expectedAddressChange), changes);
        }

        @Test
        public void testGetChangesLitigationFriend_MultipleChanges() {
            LitigationFriend current = LitigationFriend.builder()
                .firstName("John")
                .lastName("Doe")
                .primaryAddress(Address.builder().addressLine1("123 Main St").postTown("City").country("Country").build())
                .build();

            LitigationFriend updated = LitigationFriend.builder()
                .firstName("Jane")
                .lastName("Smith")
                .primaryAddress(Address.builder().addressLine1("999 Elm St").postTown("Town").country("Country").build())
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedNameChange = PartyDetailsChange.builder()
                .fieldName("Name")
                .previousValue("John Doe")
                .updatedValue("Jane Smith")
                .build();

            PartyDetailsChange expectedAddressChange = PartyDetailsChange.builder()
                .fieldName("Address")
                .previousValue("123 Main St, City, Country")
                .updatedValue("999 Elm St, Town, Country")
                .build();

            assertEquals(List.of(expectedNameChange, expectedAddressChange), changes);
        }
    }
}
