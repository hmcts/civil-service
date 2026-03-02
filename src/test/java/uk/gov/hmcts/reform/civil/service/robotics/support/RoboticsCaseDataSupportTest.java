package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class RoboticsCaseDataSupportTest {

    private final RoboticsCaseDataSupport support =
        new RoboticsCaseDataSupport(new RoboticsAddressMapper(new AddressLinesMapper()), new RoboticsPartyLookup());

    @Test
    void buildLitigiousParty_populatesCoreFields() {
        Address primaryAddress = new Address();
        primaryAddress.setAddressLine1("10 Street");
        primaryAddress.setPostCode("AB1 1AB");

        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualFirstName("John");
        party.setIndividualLastName("Doe");
        party.setIndividualDateOfBirth(LocalDate.of(1990, 1, 1));
        party.setPrimaryAddress(primaryAddress);

        LitigiousParty litigiousParty = support.buildLitigiousParty(
            party,
            null,
            "Claimant",
            "001",
            "SOL1",
            "ORG1",
            LocalDate.of(2023, 5, 1)
        );

        assertThat(litigiousParty.getId()).isEqualTo("001");
        assertThat(litigiousParty.getSolicitorID()).isEqualTo("SOL1");
        assertThat(litigiousParty.getType()).isEqualTo("Claimant");
        assertThat(litigiousParty.getName()).isEqualTo("John Doe");
        assertThat(litigiousParty.getDateOfBirth()).isEqualTo("1990-01-01");
        assertThat(litigiousParty.getDateOfService()).isEqualTo("2023-05-01");
        assertThat(litigiousParty.getSolicitorOrganisationID()).isEqualTo("ORG1");
        assertThat(litigiousParty.getAddresses().getContactAddress().getAddressLine1()).isEqualTo("10 Street");
        assertThat(litigiousParty.getAddresses().getContactAddress().getPostCode()).isEqualTo("AB1 1AB");
    }

    @Test
    void applyOrganisationDetails_copiesFields() {
        SolicitorOrganisationDetails organisationDetails = new SolicitorOrganisationDetails()
            .setOrganisationName("Org Name")
            .setPhoneNumber("01234 567890")
            .setFax("01234 098765")
            .setDx("DX 123")
            .setEmail("contact@example.com")
            .setAddress(new Address("1 High Street", null, null, null, null, null, "ZZ1 1ZZ"))
            ;

        Solicitor.SolicitorBuilder<?, ?> builder = Solicitor.builder();

        support.applyOrganisationDetails(builder, organisationDetails);
        Solicitor solicitor = builder.build();

        assertThat(solicitor.getName()).isEqualTo("Org Name");
        assertThat(solicitor.getContactTelephoneNumber()).isEqualTo("01234 567890");
        assertThat(solicitor.getContactFaxNumber()).isEqualTo("01234 098765");
        assertThat(solicitor.getContactDX()).isEqualTo("DX 123");
        assertThat(solicitor.getContactEmailAddress()).isEqualTo("contact@example.com");
        assertThat(solicitor.getAddresses().getContactAddress().getAddressLine1()).isEqualTo("1 High Street");
    }

    @Test
    void applyOrganisation_usesProvidedAddressAndDx() {
        ContactInformation contactInformation = new ContactInformation()
            .setAddressLine1("Contact Line 1")
            .setPostCode("AA1 1AA")
            .setDxAddress(List.of(new DxAddress().setDxNumber("DX 999")));
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation = mock(
            uk.gov.hmcts.reform.civil.prd.model.Organisation.class);
        when(organisation.getName()).thenReturn("Organisation Ltd");
        when(organisation.getContactInformation()).thenReturn(List.of(contactInformation));
        Address providedServiceAddress = new Address("Provided Line 1", null, null, null, null, null, "BB1 1BB");

        Solicitor.SolicitorBuilder<?, ?> builder = Solicitor.builder();
        support.applyOrganisation(builder, organisation, providedServiceAddress);
        Solicitor solicitor = builder.build();

        assertThat(solicitor.getName()).isEqualTo("Organisation Ltd");
        assertThat(solicitor.getContactDX()).isEqualTo("DX 999");
        assertThat(solicitor.getAddresses().getContactAddress().getAddressLine1()).isEqualTo("Provided Line 1");
        assertThat(solicitor.getAddresses().getContactAddress().getPostCode()).isEqualTo("BB1 1BB");
    }

    @Test
    void resolveOrganisationAddresses_fallsBackToContactInformation() {
        ContactInformation contactInformation = new ContactInformation()
            .setAddressLine1("Contact Line 1")
            .setPostCode("AA1 1AA");

        RoboticsAddresses addresses = support.resolveOrganisationAddresses(List.of(contactInformation), null);

        assertThat(addresses.getContactAddress().getAddressLine1()).isEqualTo("Contact Line 1");
        assertThat(addresses.getContactAddress().getPostCode()).isEqualTo("AA1 1AA");
    }

    @Test
    void applyOrganisationUsesContactInformationWhenNoServiceAddress() {
        ContactInformation contactInformation = new ContactInformation()
            .setAddressLine1("Contact Line 1")
            .setPostCode("AA1 1AA")
            .setDxAddress(List.of(new DxAddress().setDxNumber("DX 555")));
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation = mock(
            uk.gov.hmcts.reform.civil.prd.model.Organisation.class);
        when(organisation.getName()).thenReturn("Organisation Ltd");
        when(organisation.getContactInformation()).thenReturn(List.of(contactInformation));

        Solicitor.SolicitorBuilder<?, ?> builder = Solicitor.builder();
        support.applyOrganisation(builder, organisation, null);
        Solicitor solicitor = builder.build();

        assertThat(solicitor.getAddresses().getContactAddress().getAddressLine1()).isEqualTo("Contact Line 1");
        assertThat(solicitor.getContactDX()).isEqualTo("DX 555");
    }

    @Test
    void resolveDxReturnsNullWhenContactInfoMissingOrEmpty() {
        assertThat(support.resolveDx(null)).isNull();
        assertThat(support.resolveDx(List.of(new ContactInformation()))).isNull();
    }

    @Test
    void buildSolicitor_appliesOrganisationAndDetails() {
        ContactInformation contactInformation = new ContactInformation()
            .setAddressLine1("Org Line 1")
            .setPostCode("AB1 2CD")
            .setDxAddress(List.of(new DxAddress().setDxNumber("DX 111")));
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation = mock(
            uk.gov.hmcts.reform.civil.prd.model.Organisation.class);
        when(organisation.getName()).thenReturn("Organisation Ltd");
        when(organisation.getContactInformation()).thenReturn(List.of(contactInformation));

        SolicitorOrganisationDetails organisationDetails = new SolicitorOrganisationDetails()
            .setOrganisationName("Override Org")
            .setPhoneNumber("01234 567890")
            .setEmail("org@example.com")
            .setDx("DX 222")
            .setAddress(new Address("Details Line 1", null, null, null, null, null, "YY1 2YY"))
            ;

        Address serviceAddress = new Address("Service Line 1", null, null, null, null, null, "ZZ1 9ZZ");

        Solicitor solicitor = support.buildSolicitor(
            RoboticsCaseDataSupport.SolicitorData.builder()
                .id("SOL1")
                .isPayee(true)
                .organisationId("ORG1")
                .contactEmailAddress("solicitor@example.com")
                .reference("REF123")
                .serviceAddress(serviceAddress)
                .organisation(organisation)
                .organisationDetails(organisationDetails)
                .build()
        );

        assertThat(solicitor.getId()).isEqualTo("SOL1");
        assertThat(solicitor.getOrganisationId()).isEqualTo("ORG1");
        assertThat(solicitor.getContactEmailAddress()).isEqualTo("org@example.com");
        assertThat(solicitor.getContactTelephoneNumber()).isEqualTo("01234 567890");
        assertThat(solicitor.getContactDX()).isEqualTo("DX 222");
        assertThat(solicitor.getAddresses().getContactAddress().getAddressLine1()).isEqualTo("Details Line 1");
        assertThat(solicitor.getAddresses().getContactAddress().getPostCode()).isEqualTo("YY1 2YY");
        assertThat(solicitor.getReference()).isEqualTo("REF123");
        assertThat(solicitor.isPayee()).isTrue();
    }

    @Test
    void buildSolicitor_truncatesReferenceOverLimit() {
        Solicitor solicitor = support.buildSolicitor(
            RoboticsCaseDataSupport.SolicitorData.builder()
                .id("SOL1")
                .isPayee(false)
                .organisationId(null)
                .reference("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                .build()
        );

        assertThat(solicitor.getReference()).isEqualTo("ABCDEFGHIJKLMNOPQRSTUVWX");
    }

    @Test
    void resolveRespondentSolicitorId_handlesAllBranches() {
        assertThat(support.resolveRespondentSolicitorId(YES, YES)).isEqualTo(RoboticsDataUtil.RESPONDENT_SOLICITOR_ID);
        assertThat(support.resolveRespondentSolicitorId(YES, NO)).isEqualTo(RoboticsDataUtil.RESPONDENT2_SOLICITOR_ID);
        assertThat(support.resolveRespondentSolicitorId(NO, YES)).isNull();
        assertThat(support.resolveRespondentSolicitorId(null, YES)).isNull();
        assertThat(support.resolveRespondentSolicitorId(YES, null)).isNull();
    }
}
