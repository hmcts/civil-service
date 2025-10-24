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
        Organisation organisation = Organisation.builder()
            .name("Test Org")
            .contactInformation(Collections.emptyList())
            .build();

        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder = Solicitor.builder();

        mapper.buildOrganisation(solicitorBuilder, null).accept(organisation);

        Solicitor solicitor = solicitorBuilder.build();
        assertThat(solicitor.getName()).isEqualTo("Test Org");
        assertThat(solicitor.getContactDX()).isNull();
    }

    @Test
    void shouldGetContactDXWhenDxAddressExists() {
        DxAddress dxAddress = DxAddress.builder().dxNumber("DX123").build();
        ContactInformation contact = ContactInformation.builder()
            .dxAddress(List.of(dxAddress))
            .build();

        String result = mapper.getContactDX(List.of(contact));

        assertThat(result).isEqualTo("DX123");
    }

    @Test
    void shouldReturnNullWhenDxAddressMissing() {
        ContactInformation contact = ContactInformation.builder()
            .dxAddress(Collections.emptyList())
            .build();

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
        Address provided = Address.builder().build();
        RoboticsAddresses roboticsAddresses = RoboticsAddresses.builder().build();

        when(addressMapper.toRoboticsAddresses(provided)).thenReturn(roboticsAddresses);

        RoboticsAddresses result = mapper.fromProvidedAddress(Collections.emptyList(), provided);

        assertThat(result).isSameAs(roboticsAddresses);
        verify(addressMapper).toRoboticsAddresses(provided);
    }

    @Test
    void shouldUseContactInformationWhenProvidedAddressNotPresent() {
        ContactInformation contact = ContactInformation.builder().build();
        RoboticsAddresses roboticsAddresses = RoboticsAddresses.builder().build();

        when(addressMapper.toRoboticsAddresses(List.of(contact))).thenReturn(roboticsAddresses);

        RoboticsAddresses result = mapper.fromProvidedAddress(List.of(contact), null);

        assertThat(result).isSameAs(roboticsAddresses);
        verify(addressMapper).toRoboticsAddresses(List.of(contact));
    }

    @Test
    void shouldReturnOrganisationId() {
        uk.gov.hmcts.reform.ccd.model.Organisation org =
            uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("ORG123").build();
        OrganisationPolicy policy = OrganisationPolicy.builder().organisation(org).build();

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
        SolicitorOrganisationDetails orgDetails = SolicitorOrganisationDetails.builder()
            .organisationName("Solicitor Org")
            .phoneNumber("012345")
            .fax("fax123")
            .dx("dx123")
            .email("email@test.com")
            .address(Address.builder().build())
            .build();

        RoboticsAddresses roboticsAddresses = RoboticsAddresses.builder().build();
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
