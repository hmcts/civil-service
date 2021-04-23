package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.DxAddress;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.unspec.assertion.CustomAssertions;
import uk.gov.hmcts.reform.unspec.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.OrganisationService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistoryMapper.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class,
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
        .organisationIdentifier("QWERTY")
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
    PrdAdminUserConfiguration userConfig;

    @BeforeEach
    void setUp() {
        given(organisationApi.findOrganisationById(any(), any(), any())).willReturn(ORGANISATION);
    }

    @Autowired
    RoboticsDataMapper mapper;

    @Test
    void shouldMapToRoboticsCaseData_whenHandOffPointIsUnrepresentedDefendant() {
        CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();

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
        roboticsCaseData.getSolicitors()
            .stream()
            .forEach(
                solicitor -> {
                    assertThat(solicitor.getOrganisationId())
                        .isEqualTo("QWERTY");
                    assertThat(solicitor.getName())
                        .isEqualTo("Org Name");
                    assertThat(solicitor.getContactDX())
                        .isEqualTo("DX 12345");
                    CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
                        .isEqualTo(solicitor.getAddresses().getContactAddress());
                });
    }

    @Test
    void shouldMapToRoboticsCaseData_whenOrganisationPolicyIsNotPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant()
            .respondent1OrganisationPolicy(null).respondentSolicitor1OrganisationDetails(null).build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(1);
        var applicantSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(applicantSolicitor.getOrganisationId()).isEqualTo("QWERTY");
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
