package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BaseRoboticsDataMapperTest {

    @Mock
    private RoboticsAddressMapper addressMapper;

    private BaseRoboticsDataMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new BaseRoboticsDataMapper(addressMapper) {};
    }

    @Test
    void shouldBuildOrganisation() {
        Organisation organisation = new Organisation()
            .setName("Test Org")
            .setContactInformation(Collections.emptyList());

        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder = Solicitor.builder();

        mapper.buildOrganisation(solicitorBuilder, null).accept(organisation);

        Solicitor solicitor = solicitorBuilder.build();
        assertThat(solicitor.getName()).isEqualTo("Test Org");
        assertThat(solicitor.getContactDX()).isNull();
    }

    @Test
    void shouldGetContactDXWhenDxAddressExists() {
        DxAddress dxAddress = new DxAddress().setDxNumber("DX123");
        ContactInformation contact = new ContactInformation()
            .setDxAddress(List.of(dxAddress));

        String result = mapper.getContactDX(List.of(contact));

        assertThat(result).isEqualTo("DX123");
    }

    @Test
    void shouldReturnNullWhenDxAddressMissing() {
        ContactInformation contact = new ContactInformation()
            .setDxAddress(Collections.emptyList());

        String result = mapper.getContactDX(List.of(contact));

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenContactInformationMissing() {
        String result = mapper.getContactDX(null);

        assertThat(result).isNull();
    }

    @Test
    void shouldUseProvidedAddressWhenPresent() {
        Address provided = new Address();
        RoboticsAddresses roboticsAddresses = new RoboticsAddresses();

        when(addressMapper.toRoboticsAddresses(provided)).thenReturn(roboticsAddresses);

        RoboticsAddresses result = mapper.fromProvidedAddress(Collections.emptyList(), provided);

        assertThat(result).isSameAs(roboticsAddresses);
        verify(addressMapper).toRoboticsAddresses(provided);
    }

    @Test
    void shouldUseContactInformationWhenProvidedAddressNotPresent() {
        ContactInformation contact = new ContactInformation();
        RoboticsAddresses roboticsAddresses = new RoboticsAddresses();

        when(addressMapper.toRoboticsAddresses(List.of(contact))).thenReturn(roboticsAddresses);

        RoboticsAddresses result = mapper.fromProvidedAddress(List.of(contact), null);

        assertThat(result).isSameAs(roboticsAddresses);
        verify(addressMapper).toRoboticsAddresses(List.of(contact));
    }

    @Test
    void shouldReturnOrganisationId() {
        uk.gov.hmcts.reform.ccd.model.Organisation org =
            new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("ORG123");
        OrganisationPolicy policy = new OrganisationPolicy();
        policy.setOrganisation(org);

        Optional<String> result = mapper.getOrganisationId(policy);

        assertThat(result).contains("ORG123");
    }

    @Test
    void shouldReturnEmptyWhenOrganisationPolicyIsNull() {
        Optional<String> result = mapper.getOrganisationId(null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldBuildOrganisationDetails() {
        SolicitorOrganisationDetails orgDetails = new SolicitorOrganisationDetails();
        orgDetails.setOrganisationName("Solicitor Org");
        orgDetails.setPhoneNumber("012345");
        orgDetails.setFax("fax123");
        orgDetails.setDx("dx123");
        orgDetails.setEmail("email@test.com");
        orgDetails.setAddress(new Address());

        RoboticsAddresses roboticsAddresses = new RoboticsAddresses();
        when(addressMapper.toRoboticsAddresses(orgDetails.getAddress())).thenReturn(roboticsAddresses);

        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder = Solicitor.builder();

        mapper.buildOrganisationDetails(solicitorBuilder).accept(orgDetails);

        Solicitor solicitor = solicitorBuilder.build();
        assertThat(solicitor.getName()).isEqualTo("Solicitor Org");
        assertThat(solicitor.getContactTelephoneNumber()).isEqualTo("012345");
        assertThat(solicitor.getContactFaxNumber()).isEqualTo("fax123");
        assertThat(solicitor.getContactDX()).isEqualTo("dx123");
        assertThat(solicitor.getContactEmailAddress()).isEqualTo("email@test.com");
        assertThat(solicitor.getAddresses()).isEqualTo(roboticsAddresses);
    }
}
