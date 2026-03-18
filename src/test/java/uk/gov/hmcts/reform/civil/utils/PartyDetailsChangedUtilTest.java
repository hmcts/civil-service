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
            CaseData current = new CaseData()
                .applicant1(new PartyBuilder().individual().build())
                .build();
            CaseData updated = new CaseData()
                .applicant1(new PartyBuilder().individual().build())
                .build();

            assertNull(partyDetailsChangedUtil.buildChangesEvent(current, updated));
        }

        @Test
        public void testBuildChangesEvent_Applicant1Changes() {
            CaseData current = new CaseData()
                .applicant1(new PartyBuilder().company().build().setCompanyName("Company One"))
                .build();
            CaseData updated = new CaseData()
                .applicant1(new PartyBuilder().company().build().setCompanyName("Company Two"))
                .build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 1 Details Changed")
                .setDescription("Name: From 'Company One' to 'Company Two'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2Changes() {
            CaseData current = new CaseData()
                .applicant2(new PartyBuilder().individual().build()
                                .setIndividualFirstName("John").setIndividualLastName("Doe"))
                .build();

            CaseData updated = new CaseData()
                .applicant2(new PartyBuilder().individual().build()
                                .setIndividualFirstName("Jane").setIndividualLastName("Smith"))
                .build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 2 Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1Changes() {
            CaseData current = new CaseData()
                .respondent1(new PartyBuilder().individual().build()
                                 .setIndividualFirstName("John").setIndividualLastName("Doe"))
                .build();

            CaseData updated = new CaseData()
                .respondent1(new PartyBuilder().individual().build()
                                 .setIndividualFirstName("Jane").setIndividualLastName("Smith"))
                .build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(current, updated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 1 Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent2Changes() {
            CaseData current = new CaseData()
                .respondent2(new PartyBuilder().individual().build()
                                 .setIndividualFirstName("John").setIndividualLastName("Doe"))
                .build();

            CaseData updated = new CaseData()
                .respondent2(new PartyBuilder().individual().build()
                                 .setIndividualFirstName("Jane").setIndividualLastName("Smith"))
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

            CaseData caseDataCurrent = new CaseData().applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData().applicant1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 1 Litigation Friend Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant1LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = new Party().setPrimaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country"));

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            CaseData caseDataCurrent = new CaseData()
                .applicant1(litigant)
                .applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData()
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
            Party litigant = new Party().setPrimaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country"));

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = new CaseData()
                .applicant1(litigant)
                .applicant1LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData()
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

            CaseData caseDataCurrent = new CaseData().applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData().applicant2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Applicant 2 Litigation Friend Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Applicant2LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = new Party().setPrimaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country"));

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            CaseData caseDataCurrent = new CaseData()
                .applicant2(litigant)
                .applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData()
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
            Party litigant = new Party().setPrimaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country"));

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = new CaseData()
                .applicant2(litigant)
                .applicant2LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData()
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

            CaseData caseDataCurrent = new CaseData().respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData().respondent1LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 1 Litigation Friend Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }

        @Test
        public void testBuildChangesEvent_Respondent1LitigationFriendAddressChanges_toAddressSameAsLitigant() {
            Party litigant = new Party().setPrimaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country"));

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

            CaseData caseDataCurrent = new CaseData()
                .respondent1(litigant)
                .respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData()
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
            Party litigant = new Party().setPrimaryAddress(
                address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country"));

            LitigationFriend current = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.YES)
                ;

            LitigationFriend updated = new LitigationFriend().setFirstName("John")
                .setLastName("Doe")
                .setHasSameAddressAsLitigant(YesOrNo.NO)
                .setPrimaryAddress(address("123 Main St", "City", "Country"))
                ;

            CaseData caseDataCurrent = new CaseData()
                .respondent1(litigant)
                .respondent1LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData()
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

            CaseData caseDataCurrent = new CaseData().respondent2LitigationFriend(current).build();
            CaseData caseDataUpdated = new CaseData().respondent2LitigationFriend(updated).build();

            ContactDetailsUpdatedEvent actualEvent = partyDetailsChangedUtil.buildChangesEvent(caseDataCurrent, caseDataUpdated);

            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent()
                .setSummary("Respondent 2 Litigation Friend Details Changed")
                .setDescription("Name: From 'John Doe' to 'Jane Smith'.");

            assertEquals(expectedEvent, actualEvent);
        }
    }

    @Test
    public void testBuildChangesEvent_Respondent2LitigationFriendAddressChanges_toAddressSameAsLitigant() {
        Party litigant = new Party().setPrimaryAddress(
            address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country"));

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

        CaseData caseDataCurrent = new CaseData()
            .respondent2(litigant)
            .respondent2LitigationFriend(current).build();
        CaseData caseDataUpdated = new CaseData()
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
        Party litigant = new Party().setPrimaryAddress(
            address("Litigant Street", "Litigant City", "Litigant Postcode", "Litigant Country"));

        LitigationFriend current = new LitigationFriend().setFirstName("John")
            .setLastName("Doe")
            .setHasSameAddressAsLitigant(YesOrNo.YES)
            ;

        LitigationFriend updated = new LitigationFriend().setFirstName("John")
            .setLastName("Doe")
            .setHasSameAddressAsLitigant(YesOrNo.NO)
            .setPrimaryAddress(address("123 Main St", "City", "Country"))
            ;

        CaseData caseDataCurrent = new CaseData()
            .respondent2(litigant)
            .respondent2LitigationFriend(current).build();
        CaseData caseDataUpdated = new CaseData()
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
            Party current = new PartyBuilder().individual().build();
            Party updated = new PartyBuilder().individual().build();

            List<PartyDetailsChange> changes = partyDetailsChangedUtil.getChanges(current, updated);

            assertTrue(changes.isEmpty());
        }

        @Test
        public void testGetChangesParty_PartyNameChange_Individual() {
            Party current = new PartyBuilder().individual().build()
                .setIndividualFirstName("Jane")
                .setIndividualLastName("Carver");

            Party updated = new PartyBuilder().individual().build()
                .setIndividualFirstName("Jane")
                .setIndividualLastName("Wilson");

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
            Party current = new PartyBuilder().soleTrader().build()
                .setSoleTraderFirstName("Jane")
                .setSoleTraderLastName("Carver");

            Party updated = new PartyBuilder().soleTrader().build()
                .setSoleTraderFirstName("Jane")
                .setSoleTraderLastName("Wilson");

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
            Party current = new PartyBuilder().company().build()
                .setCompanyName("Company One");

            Party updated = new PartyBuilder().company().build()
                .setCompanyName("Company Two");

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
            Party current = new PartyBuilder().organisation().build()
                .setOrganisationName("Organisation One");

            Party updated = new PartyBuilder().organisation().build()
                .setOrganisationName("Organisation Two");

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
            Party current = new PartyBuilder().individual().build()
                .setPrimaryAddress(address("123 Main St", "City", "Country"));

            Party updated = new PartyBuilder().individual().build()
                .setPrimaryAddress(address("999 Main St", "City", "Country"));

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
            Party current = new PartyBuilder().individual().build()
                .setIndividualFirstName("John")
                .setIndividualLastName("Doe")
                .setPrimaryAddress(address("123 Main St", "City", "Country"));

            Party updated = new PartyBuilder().individual().build()
                .setIndividualFirstName("Jane")
                .setIndividualLastName("Smith")
                .setPrimaryAddress(address("999 Elm St", "Town", "Country"));

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
