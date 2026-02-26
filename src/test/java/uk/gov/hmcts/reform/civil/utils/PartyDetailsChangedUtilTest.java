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
            CaseData current = CaseData.builder()
                .applicant1(PartyBuilder.builder().individual().build())
                .build();
            CaseData updated = CaseData.builder()
                .applicant1(PartyBuilder.builder().individual().build())
                .build();

            assertNull(partyDetailsChangedUtil.buildChangesEvent(current, updated));
        }

        @Test
        public void testBuildChangesEvent_Applicant1Changes() {
            CaseData current = CaseData.builder()
                .applicant1(PartyBuilder.builder().company().build().toBuilder().companyName("Company One").build())
                .build();
            CaseData updated = CaseData.builder()
                .applicant1(PartyBuilder.builder().company().build().toBuilder().companyName("Company Two").build())
                .build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 1 Details Changed")
                .setDescription("Name: From 'Company One' to 'Company Two'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2Changes() {
            CaseData current = CaseData.builder()
                .applicant2(PartyBuilder.builder().individual().build().toBuilder()
                                .individualFirstName("John").individualLastName("Doe").build())
                .build();

            CaseData updated = CaseData.builder()
                .applicant2(PartyBuilder.builder().individual().build().toBuilder()
                                .individualFirstName("Jane").individualLastName("Smith").build())
                .build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 2 Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1Changes() {
            CaseData current = CaseData.builder()
                .respondent1(PartyBuilder.builder().individual().build().toBuilder()
                                 .individualFirstName("John").individualLastName("Doe").build())
                .build();

            CaseData updated = CaseData.builder()
                .respondent1(PartyBuilder.builder().individual().build().toBuilder()
                                 .individualFirstName("Jane").individualLastName("Smith").build())
                .build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 1 Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent2Changes() {
            CaseData current = CaseData.builder()
                .respondent2(PartyBuilder.builder().individual().build().toBuilder()
                                 .individualFirstName("John").individualLastName("Doe").build())
                .build();

            CaseData updated = CaseData.builder()
                .respondent2(PartyBuilder.builder().individual().build().toBuilder()
                                 .individualFirstName("Jane").individualLastName("Smith").build())
                .build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 2 Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant1LitigationFriendChanges() {
            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("Jane")
                .setLastName("Smith")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = CaseData.builder().applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder().applicant1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 1 Litigation Friend Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant1LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country")).build();

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            CaseData caseDataCurrent = CaseData.builder()
                .applicant1(litigant)
                .applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .applicant1(litigant)
                .applicant1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 1 Litigation Friend Details Changed")
                .setDescription("Address: From '123 Main St, City, Country' to 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant1LitigationFriendAddressChanges_toAddressNotSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country")).build();

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = CaseData.builder()
                .applicant1(litigant)
                .applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .applicant1(litigant)
                .applicant1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 1 Litigation Friend Details Changed")
                .setDescription("Address: From 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country' to '123 Main St, City, Country'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2LitigationFriendChanges() {
            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("Jane")
                .setLastName("Smith")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = CaseData.builder().applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder().applicant2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 2 Litigation Friend Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country")).build();

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            CaseData caseDataCurrent = CaseData.builder()
                .applicant2(litigant)
                .applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .applicant2(litigant)
                .applicant2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 2 Litigation Friend Details Changed")
                .setDescription("Address: From '123 Main St, City, Country' to 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2LitigationFriendAddressChanges_toAddressNotSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country")).build();

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = CaseData.builder()
                .applicant2(litigant)
                .applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .applicant2(litigant)
                .applicant2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 2 Litigation Friend Details Changed")
                .setDescription("Address: From 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country' to '123 Main St, City, Country'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1LitigationFriendChanges() {
            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("Jane")
                .setLastName("Smith")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = CaseData.builder().respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder().respondent1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 1 Litigation Friend Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country")).build();

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(
                    address("123 Main St", "City", "YO1 ERP", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            CaseData caseDataCurrent = CaseData.builder()
                .respondent1(litigant)
                .respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .respondent1(litigant)
                .respondent1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 1 Litigation Friend Details Changed")
                .setDescription("Address: From '123 Main St, City, YO1 ERP, Country' to 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1LitigationFriendAddressChanges_toAddressNotSameAsLitigant() {
            Party litigant = Party.builder().primaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country")).build();

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = CaseData.builder()
                .respondent1(litigant)
                .respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder()
                .respondent1(litigant)
                .respondent1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 1 Litigation Friend Details Changed")
                .setDescription("Address: From 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country' to '123 Main St, City, Country'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent2LitigationFriendChanges() {
            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("Jane")
                .setLastName("Smith")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = CaseData.builder().respondent2LitigationFriend(current).build();
            CaseData caseDataUpdated = CaseData.builder().respondent2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 2 Litigation Friend Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }
    }

    @Test
    public void testBuildChangesEvent_Respondent2LitigationFriendAddressChanges_toAddressSameAsLitigant() {
        Party litigant = Party.builder().primaryAddress(
            address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country")).build();

        LitigationFriend current = new LitigationFriend().setFirstName("John")
            .setLastName("Doe")
            .setHasSameAddressAsLitigant(YesOrNo.NO)
            .setPrimaryAddress(
                address("123 Main St", "City", "YO1 ERP", "Country"))
            ;

        LitigationFriend updated = new LitigationFriend().setFirstName("John")
            .setLastName("Doe")
            .setHasSameAddressAsLitigant(YesOrNo.YES)
            ;

        CaseData caseDataCurrent = CaseData.builder()
            .respondent2(litigant)
            .respondent2LitigationFriend(current).build();
        CaseData caseDataUpdated = CaseData.builder()
            .respondent2(litigant)
            .respondent2LitigationFriend(updated).build();

        ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

        ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
            .setSummary("Respondent 2 Litigation Friend Details Changed")
            .setDescription("Address: From '123 Main St, City, YO1 ERP, Country' to 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country'.");

        assertEquals(expectedEvent, actualEvent);
    }

    @Test
    public void testBuildChangesEvent_Respondent2LitigationFriendAddressChanges_toAddressNotSameAsLitigant() {
        Party litigant = Party.builder().primaryAddress(
            address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country")).build();

        LitigationFriend current = new LitigationFriend().setFirstName("John")
            .setLastName("Doe")
            .setHasSameAddressAsLitigant(YesOrNo.YES)
            ;

        LitigationFriend updated = new LitigationFriend().setFirstName("John")
            .setLastName("Doe")
            .setHasSameAddressAsLitigant(YesOrNo.NO)
            .setPrimaryAddress(address("123 Main St", "City", "Country"))
            ;

        CaseData caseDataCurrent = CaseData.builder()
            .respondent2(litigant)
            .respondent2LitigationFriend(current).build();
        CaseData caseDataUpdated = CaseData.builder()
            .respondent2(litigant)
            .respondent2LitigationFriend(updated).build();

        ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

        ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
            .setSummary("Respondent 2 Litigation Friend Details Changed")
            .setDescription("Address: From 'Litigant Street, Litigant City, Litigant Postcode, Litigant Country' to '123 Main St, City, Country'.");

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

            PartyDetailsChange expectedNameChange = new PartyDetailsChange()
                .setFieldName("Name")
                .setPreviousValue("Jane Carver")
                .setUpdatedValue("Jane Wilson")
                ;

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

            PartyDetailsChange expectedNameChange = new PartyDetailsChange()
                .setFieldName("Name")
                .setPreviousValue("Jane Carver")
                .setUpdatedValue("Jane Wilson")
                ;

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

            PartyDetailsChange expectedNameChange = new PartyDetailsChange()
                .setFieldName("Name")
                .setPreviousValue("Company One")
                .setUpdatedValue("Company Two")
                ;

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

            PartyDetailsChange expectedNameChange = new PartyDetailsChange()
                .setFieldName("Name")
                .setPreviousValue("Organisation One")
                .setUpdatedValue("Organisation Two")
                ;

            assertEquals(List.of(expectedNameChange), changes);
        }

        @Test
        public void testGetChangesParty_AddressChange() {
            Party current = PartyBuilder.builder().individual().build().toBuilder()
                .primaryAddress(address("123 Main St", "City", "Country"))
                .build();

            Party updated = PartyBuilder.builder().individual().build().toBuilder()
                .primaryAddress(address("999 Main St", "City", "Country"))
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedAddressChange = new PartyDetailsChange()
                .setFieldName("Address")
                .setPreviousValue("123 Main St, City, Country")
                .setUpdatedValue("999 Main St, City, Country")
                ;

            assertEquals(List.of(expectedAddressChange), changes);
        }

        @Test
        public void testGetChangesParty_MultipleChanges() {
            Party current = PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("John")
                .individualLastName("Doe")
                .primaryAddress(address("123 Main St", "City", "Country"))
                .build();

            Party updated = PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("Jane")
                .individualLastName("Smith")
                .primaryAddress(address("999 Elm St", "Town", "Country"))
                .build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedNameChange = new PartyDetailsChange()
                .setFieldName("Name")
                .setPreviousValue("John Doe")
                .setUpdatedValue("Jane Smith")
                ;

            PartyDetailsChange expectedAddressChange = new PartyDetailsChange()
                .setFieldName("Address")
                .setPreviousValue("123 Main St, City, Country")
                .setUpdatedValue("999 Elm St, Town, Country")
                ;

            assertEquals(List.of(expectedNameChange, expectedAddressChange), changes);
        }
    }

    @Nested
    class GetChangesLitigationFriendTests {

        @Test
        public void testGetChangesLitigationFriend_NoChanges() {
            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);
            assertTrue(changes.isEmpty());
        }

        @Test
        public void testGetChangesLitigationFriend_NameChange() {
            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("Jane")
                .setLastName("Smith")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedChange = new PartyDetailsChange()
                .setFieldName("Name")
                .setPreviousValue("John Doe")
                .setUpdatedValue("Jane Smith")
                ;

            assertEquals(List.of(expectedChange), changes);
        }

        @Test
        public void testGetChangesLitigationFriend_AddressChange() {
            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("999 Elm St", "Town", "Country"))
                ;

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedAddressChange = new PartyDetailsChange()
                .setFieldName("Address")
                .setPreviousValue("123 Main St, City, Country")
                .setUpdatedValue("999 Elm St, Town, Country")
                ;

            assertEquals(List.of(expectedAddressChange), changes);
        }

        @Test
        public void testGetChangesLitigationFriend_MultipleChanges() {
            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("Jane")
                .setLastName("Smith")
                .setPrimaryAddress(address("999 Elm St", "Town", "Country"))
                ;

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            PartyDetailsChange expectedNameChange = new PartyDetailsChange()
                .setFieldName("Name")
                .setPreviousValue("John Doe")
                .setUpdatedValue("Jane Smith")
                ;

            PartyDetailsChange expectedAddressChange = new PartyDetailsChange()
                .setFieldName("Address")
                .setPreviousValue("123 Main St, City, Country")
                .setUpdatedValue("999 Elm St, Town, Country")
                ;

            assertEquals(List.of(expectedNameChange, expectedAddressChange), changes);
        }
    }

    private static Address address(String addressLine1, String postTown, String country) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setPostTown(postTown);
        address.setCountry(country);
        return address;
    }

    private static Address address(String addressLine1, String postTown, String postCode, String country) {
        Address address = address(addressLine1, postTown, country);
        address.setPostCode(postCode);
        return address;
    }
}
