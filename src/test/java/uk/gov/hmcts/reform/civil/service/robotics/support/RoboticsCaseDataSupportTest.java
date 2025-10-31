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
        Party party = Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualFirstName("John")
            .individualLastName("Doe")
            .individualDateOfBirth(LocalDate.of(1990, 1, 1))
            .primaryAddress(Address.builder()
                .addressLine1("10 Street")
                .postCode("AB1 1AB")
                .build())
            .build();

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
        SolicitorOrganisationDetails organisationDetails = SolicitorOrganisationDetails.builder()
            .organisationName("Org Name")
            .phoneNumber("01234 567890")
            .fax("01234 098765")
            .dx("DX 123")
            .email("contact@example.com")
            .address(Address.builder()
                .addressLine1("1 High Street")
                .postCode("ZZ1 1ZZ")
                .build())
            .build();

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
        ContactInformation contactInformation = ContactInformation.builder()
            .addressLine1("Contact Line 1")
            .postCode("AA1 1AA")
            .dxAddress(List.of(DxAddress.builder().dxNumber("DX 999").build()))
            .build();
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation = mock(
            uk.gov.hmcts.reform.civil.prd.model.Organisation.class);
        when(organisation.getName()).thenReturn("Organisation Ltd");
        when(organisation.getContactInformation()).thenReturn(List.of(contactInformation));
        Address providedServiceAddress = Address.builder()
            .addressLine1("Provided Line 1")
            .postCode("BB1 1BB")
            .build();

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
        ContactInformation contactInformation = ContactInformation.builder()
            .addressLine1("Contact Line 1")
            .postCode("AA1 1AA")
            .build();

        RoboticsAddresses addresses = support.resolveOrganisationAddresses(List.of(contactInformation), null);

        assertThat(addresses.getContactAddress().getAddressLine1()).isEqualTo("Contact Line 1");
        assertThat(addresses.getContactAddress().getPostCode()).isEqualTo("AA1 1AA");
    }

    @Test
    void buildSolicitor_appliesOrganisationAndDetails() {
        ContactInformation contactInformation = ContactInformation.builder()
            .addressLine1("Org Line 1")
            .postCode("AB1 2CD")
            .dxAddress(List.of(DxAddress.builder().dxNumber("DX 111").build()))
            .build();
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation = mock(
            uk.gov.hmcts.reform.civil.prd.model.Organisation.class);
        when(organisation.getName()).thenReturn("Organisation Ltd");
        when(organisation.getContactInformation()).thenReturn(List.of(contactInformation));

        SolicitorOrganisationDetails organisationDetails = SolicitorOrganisationDetails.builder()
            .organisationName("Override Org")
            .phoneNumber("01234 567890")
            .email("org@example.com")
            .dx("DX 222")
            .address(Address.builder()
                .addressLine1("Details Line 1")
                .postCode("YY1 2YY")
                .build())
            .build();

        Address serviceAddress = Address.builder()
            .addressLine1("Service Line 1")
            .postCode("ZZ1 9ZZ")
            .build();

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
