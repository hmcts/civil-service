package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.assertion.CustomAssertions;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.DxAddress;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistorySequencer.class,
    EventHistoryMapper.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class,
    OrganisationService.class
})
@ExtendWith(SpringExtension.class)
class RoboticsDataMapperTest {

    private static final ContactInformation CONTACT_INFORMATION = ContactInformation.builder()
        .addressLine1("line 1")
        .addressLine2("line 2")
        .postCode("AB1 2XY")
        .county("My county")
        .dxAddress(List.of(DxAddress.builder()
                               .dxNumber("DX 12345")
                               .build()))
        .build();
    private static final Organisation ORGANISATION = Organisation.builder()
        .organisationIdentifier("QWERTY R")
        .name("Org Name")
        .contactInformation(List.of(CONTACT_INFORMATION))
        .build();

    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    IdamClient idamClient;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    PrdAdminUserConfiguration userConfig;
    @MockBean
    private Time time;

    LocalDateTime localDateTime;

    @BeforeEach
    void setUp() {
        given(organisationApi.findOrganisationById(any(), any(), any())).willReturn(ORGANISATION);
        localDateTime = LocalDateTime.of(2020, 8, 1, 12, 0, 0);
        when(time.now()).thenReturn(localDateTime);
    }

    @Autowired
    RoboticsDataMapper mapper;

    @Test
    void shouldMapToRoboticsCaseData_whenHandOffPointIsUnrepresentedDefendant() {
        CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendants().build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
    }

    @Test
    void shouldMapToRoboticsCaseData_whenDefendantIsNotRegistered() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePaymentSuccessful()
            .respondentSolicitor1OrganisationDetails(SolicitorOrganisationDetails.builder()
                                                         .organisationName("My Organisation")
                                                         .email("me@server.net")
                                                         .phoneNumber("0123456789")
                                                         .fax("9999999999")
                                                         .dx("Dx")
                                                         .address(Address.builder().build())
                                                         .build())
            .build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
    }

    @Test
    void shouldMapToRoboticsCaseData_whenOrganisationPolicyIsPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(2);

        var firstSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(firstSolicitor.getOrganisationId()).isEqualTo("QWERTY A");
        assertThat(firstSolicitor.getName()).isEqualTo("Org Name");
        assertThat(firstSolicitor.getContactDX()).isEqualTo("DX 12345");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(firstSolicitor.getAddresses().getContactAddress());

        var secondSolicitor = roboticsCaseData.getSolicitors().get(1);
        assertThat(secondSolicitor.getOrganisationId()).isEqualTo("QWERTY R");
        assertThat(secondSolicitor.getName()).isEqualTo("Org Name");
        assertThat(secondSolicitor.getContactDX()).isEqualTo("DX 12345");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(secondSolicitor.getAddresses().getContactAddress());
    }

    @Test
    void shouldMapToRoboticsCaseData_whenOrganisationPolicyIsPresentWithProvidedServiceAddress() {
        var solicitorServiceAddress = Address.builder()
            .addressLine1("line 1 provided")
            .addressLine2("line 2")
            .postCode("AB1 2XY")
            .county("My county")
            .build();

        ContactInformation contactInformation = CONTACT_INFORMATION.toBuilder().addressLine1("line 1 provided").build();

        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .applicantSolicitor1ServiceAddress(solicitorServiceAddress)
            .respondentSolicitor1ServiceAddress(solicitorServiceAddress)
            .build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(2);

        var firstSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(firstSolicitor.getOrganisationId()).isEqualTo("QWERTY A");
        assertThat(firstSolicitor.getName()).isEqualTo("Org Name");
        assertThat(firstSolicitor.getContactDX()).isEqualTo("DX 12345");
        CustomAssertions.assertThat(List.of(contactInformation))
            .isEqualTo(firstSolicitor.getAddresses().getContactAddress());

        var secondSolicitor = roboticsCaseData.getSolicitors().get(1);
        assertThat(secondSolicitor.getOrganisationId()).isEqualTo("QWERTY R");
        assertThat(secondSolicitor.getName()).isEqualTo("Org Name");
        assertThat(secondSolicitor.getContactDX()).isEqualTo("DX 12345");
        CustomAssertions.assertThat(List.of(contactInformation))
            .isEqualTo(secondSolicitor.getAddresses().getContactAddress());
    }

    @Test
    void shouldMapToRoboticsCaseData_whenOrganisationPolicyIsNotPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendants()
            .respondent1OrganisationPolicy(null).respondentSolicitor1OrganisationDetails(null).build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(1);
        var applicantSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(applicantSolicitor.getOrganisationId()).isEqualTo("QWERTY A");
        assertThat(applicantSolicitor.getName()).isEqualTo("Org Name");
        assertThat(applicantSolicitor.getContactDX()).isEqualTo("DX 12345");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(applicantSolicitor.getAddresses().getContactAddress());
    }

    @Test
    void shouldThrowNullPointerException_whenCaseDataIsNull() {
        assertThrows(NullPointerException.class, () ->
                         mapper.toRoboticsCaseData(null),
                     "caseData cannot be null"
        );
    }
}
