package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisation;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisationCollectionItem;
import uk.gov.hmcts.reform.civil.assertion.CustomAssertions;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.robotics.NoticeOfChange;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

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
    UserService userService;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    PrdAdminUserConfiguration userConfig;
    @MockBean
    private Time time;
    @MockBean
    LocationRefDataService locationRefDataService;
    @MockBean
    LocationRefDataUtil locationRefDataUtil;
    private static final String BEARER_TOKEN = "Bearer Token";
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

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

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
    }

    @Test
    void shouldMapToRoboticsCaseData_whenOrganisationPolicyIsPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(2);

        var firstSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(firstSolicitor.getOrganisationId()).isEqualTo("QWERTY A");
        assertThat(firstSolicitor.getName()).isEqualTo("Org Name");
        assertThat(firstSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(firstSolicitor.getContactEmailAddress()).isEqualTo("applicantsolicitor@example.com");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(firstSolicitor.getAddresses().getContactAddress());

        var secondSolicitor = roboticsCaseData.getSolicitors().get(1);
        assertThat(secondSolicitor.getOrganisationId()).isEqualTo("QWERTY R");
        assertThat(secondSolicitor.getName()).isEqualTo("Org Name");
        assertThat(secondSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(secondSolicitor.getContactEmailAddress()).isEqualTo("respondentsolicitor@example.com");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(secondSolicitor.getAddresses().getContactAddress());
    }

    @Test
    void atStatePaymentSuccessfulWithCopyOrganisationIdPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessfulWithCopyOrganisationOnly().build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(2);

        var firstSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(firstSolicitor.getOrganisationId()).isEqualTo("QWERTY A");
        assertThat(firstSolicitor.getName()).isEqualTo("Org Name");
        assertThat(firstSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(firstSolicitor.getContactEmailAddress()).isEqualTo("applicantsolicitor@example.com");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(firstSolicitor.getAddresses().getContactAddress());

        var secondSolicitor = roboticsCaseData.getSolicitors().get(1);
        assertThat(secondSolicitor.getOrganisationId()).isEqualTo("QWERTY R");
        assertThat(secondSolicitor.getName()).isEqualTo("Org Name");
        assertThat(secondSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(secondSolicitor.getContactEmailAddress()).isEqualTo("respondentsolicitor@example.com");
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

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(2);

        var firstSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(firstSolicitor.getOrganisationId()).isEqualTo("QWERTY A");
        assertThat(firstSolicitor.getName()).isEqualTo("Org Name");
        assertThat(firstSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(firstSolicitor.getContactEmailAddress()).isEqualTo("applicantsolicitor@example.com");
        CustomAssertions.assertThat(List.of(contactInformation))
            .isEqualTo(firstSolicitor.getAddresses().getContactAddress());

        var secondSolicitor = roboticsCaseData.getSolicitors().get(1);
        assertThat(secondSolicitor.getOrganisationId()).isEqualTo("QWERTY R");
        assertThat(secondSolicitor.getName()).isEqualTo("Org Name");
        assertThat(secondSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(secondSolicitor.getContactEmailAddress()).isEqualTo("respondentsolicitor@example.com");
        CustomAssertions.assertThat(List.of(contactInformation))
            .isEqualTo(secondSolicitor.getAddresses().getContactAddress());
    }

    @Test
    void shouldMapToRoboticsCaseData_whenOrganisationPolicyIsNotPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants()
            .respondent1OrganisationPolicy(null)
            .respondentSolicitor1OrganisationDetails(null)
            .respondent1OrganisationIDCopy(null).build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(1);
        var applicantSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(applicantSolicitor.getOrganisationId()).isEqualTo("QWERTY A");
        assertThat(applicantSolicitor.getName()).isEqualTo("Org Name");
        assertThat(applicantSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(applicantSolicitor.getContactEmailAddress()).isEqualTo("applicantsolicitor@example.com");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(applicantSolicitor.getAddresses().getContactAddress());
    }

    @Test
    void shouldThrowNullPointerException_whenCaseDataIsNull() {
        assertThrows(NullPointerException.class, () ->
                         mapper.toRoboticsCaseData(null, BEARER_TOKEN),
                     "caseData cannot be null"
        );
    }

    @Test
    void shouldMapToRoboticsCaseDataWhen2ndClaimantIsPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build().toBuilder()
            .applicant2(PartyBuilder.builder().individual().build())
            .addApplicant2(YES)
            .build();
        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getLitigiousParties()).hasSize(3);
    }

    @Test
    void shouldMapToRoboticsCaseDataWhen2ndDefendantIsPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build().toBuilder()
            .respondent2(PartyBuilder.builder().company().build())
            .addRespondent2(YES)
            .build();
        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getLitigiousParties()).hasSize(3);
    }

    @Test
    void shouldMapToRoboticsCaseDataWhen2ndDefendantIsRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build().toBuilder()
            .respondent2(PartyBuilder.builder().company().build())
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .build();
        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(3);
    }

    @Test
    void shouldMapToRoboticsCaseDataWhen2ndDefendantIsRepresentedSameSolicitor() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build().toBuilder()
            .respondent2(PartyBuilder.builder().company().build())
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(YES)
            .build();
        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(2);
    }

    @Test
    void shouldCheck_whenFeignException() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build().toBuilder()
            .respondent2(PartyBuilder.builder().company().build())
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(YES)
            .build();
        given(organisationApi.findOrganisationById(any(), any(), any()))
            .willThrow(Mockito.mock(FeignException.class));
        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(2);
    }

    @Test
    void shouldMapToRoboticsCaseDataWhen2ndDefendantIsRepresentedDifferentSolicitor() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build().toBuilder()
            .respondent2(PartyBuilder.builder().company().build())
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(NO)
            .build();
        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(3);

        var firstSolicitor = roboticsCaseData.getSolicitors().get(0);
        assertThat(firstSolicitor.getOrganisationId()).isEqualTo("QWERTY A");
        assertThat(firstSolicitor.getName()).isEqualTo("Org Name");
        assertThat(firstSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(firstSolicitor.getContactEmailAddress()).isEqualTo("applicantsolicitor@example.com");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(firstSolicitor.getAddresses().getContactAddress());

        var secondSolicitor = roboticsCaseData.getSolicitors().get(1);
        assertThat(secondSolicitor.getOrganisationId()).isEqualTo("QWERTY R");
        assertThat(secondSolicitor.getName()).isEqualTo("Org Name");
        assertThat(secondSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(secondSolicitor.getContactEmailAddress()).isEqualTo("respondentsolicitor@example.com");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(secondSolicitor.getAddresses().getContactAddress());

        var thirdSolicitor = roboticsCaseData.getSolicitors().get(2);
        assertThat(thirdSolicitor.getOrganisationId()).isEqualTo("QWERTY R2");
        assertThat(thirdSolicitor.getName()).isEqualTo("Org Name");
        assertThat(thirdSolicitor.getContactDX()).isEqualTo("DX 12345");
        assertThat(thirdSolicitor.getContactEmailAddress()).isEqualTo("respondentsolicitor2@example.com");
        CustomAssertions.assertThat(List.of(CONTACT_INFORMATION))
            .isEqualTo(thirdSolicitor.getAddresses().getContactAddress());
    }

    @Test
    void shouldMapToRoboticsCaseDataWhen2ndDefendantIsRepresentedNotRegistered() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build().toBuilder()
            .respondent2(PartyBuilder.builder().company().build())
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .respondent2OrgRegistered(NO)
            .build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(3);
    }

    @Test
    void shouldMapToRoboticsCaseDataWhen2ndDefendantIsNotRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build().toBuilder()
            .respondent2(PartyBuilder.builder().company().build())
            .addRespondent2(YES)
            .respondent2Represented(NO)
            .build();
        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getSolicitors()).hasSize(2);
    }

    @Test
    void shouldMapToRoboticsCaseDataWhenPreferredCourtCodeFetchedFromRefData() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
        when(locationRefDataUtil.getPreferredCourtData(any(), any(), eq(true))).thenReturn("121");

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getHeader().getPreferredCourtCode()).isEqualTo("121");
    }

    @Test
    void shouldReturnEmptyStringWhenPreferredCourtCodeisUnavailableFromLocationRefData() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
        when(locationRefDataUtil.getPreferredCourtData(any(), any(), eq(true))).thenReturn("");
        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);
        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
        assertThat(roboticsCaseData.getHeader().getPreferredCourtCode()).isEqualTo("");
    }

    @Test
    void shouldMapExpectedNoticeOfChangeData_whenCaseGoesOffline() {
        when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePaymentSuccessful()
            .build().toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .build();
        var app1NocDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");
        var res1NocDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
        var res2NocDate = LocalDateTime.parse("2022-03-01T12:00:00.000550439");

        caseData = caseData.toBuilder()
            .applicant1OrganisationPolicy(
                caseData.getApplicant1OrganisationPolicy().toBuilder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("App 1 org", app1NocDate)))
                    .build())
        .respondent1OrganisationPolicy(
            caseData.getApplicant1OrganisationPolicy().toBuilder()
                .previousOrganisations(List.of(buildPreviousOrganisation("Res 1 org", res1NocDate)))
                .build())
        .respondent2OrganisationPolicy(
            caseData.getApplicant1OrganisationPolicy().toBuilder()
                .previousOrganisations(List.of(buildPreviousOrganisation("Res 2 org", res2NocDate)))
                .build())
            .build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        assertThat(roboticsCaseData.getNoticeOfChange()).isEqualTo(
            List.of(
                NoticeOfChange.builder().litigiousPartyID("001").dateOfNoC(app1NocDate.format(ISO_DATE)).build(),
                NoticeOfChange.builder().litigiousPartyID("002").dateOfNoC(res1NocDate.format(ISO_DATE)).build(),
                NoticeOfChange.builder().litigiousPartyID("003").dateOfNoC(res2NocDate.format(ISO_DATE)).build())
        );
    }

    @Test
    void shouldMapExpectedNoticeOfChangeData_whenCaseDismissed() {
        when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePaymentSuccessful()
            .build().toBuilder()
            .ccdState(CaseState.CASE_DISMISSED)
            .build();
        var app1NocDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");
        var res1NocDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
        var res2NocDate = LocalDateTime.parse("2022-03-01T12:00:00.000550439");

        caseData = caseData.toBuilder()
            .applicant1OrganisationPolicy(
                caseData.getApplicant1OrganisationPolicy().toBuilder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("App 1 org", app1NocDate)))
                    .build())
            .respondent1OrganisationPolicy(
                caseData.getApplicant1OrganisationPolicy().toBuilder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("Res 1 org", res1NocDate)))
                    .build())
            .respondent2OrganisationPolicy(
                caseData.getApplicant1OrganisationPolicy().toBuilder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("Res 2 org", res2NocDate)))
                    .build())
            .build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        assertThat(roboticsCaseData.getNoticeOfChange()).isEqualTo(
            List.of(
                NoticeOfChange.builder().litigiousPartyID("001").dateOfNoC(app1NocDate.format(ISO_DATE)).build(),
                NoticeOfChange.builder().litigiousPartyID("002").dateOfNoC(res1NocDate.format(ISO_DATE)).build(),
                NoticeOfChange.builder().litigiousPartyID("003").dateOfNoC(res2NocDate.format(ISO_DATE)).build())
        );
    }

    @Test
    void shouldNotPopulateNoticeOfChangeSection_whenCaseIsStillOnline() {
        when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);

        var app1NocDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");
        var res1NocDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
        var res2NocDate = LocalDateTime.parse("2022-03-01T12:00:00.000550439");

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePaymentSuccessful()
            .build().toBuilder()
            .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
            .build();

        caseData = caseData.toBuilder()
            .applicant1OrganisationPolicy(
                caseData.getApplicant1OrganisationPolicy().toBuilder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("App 1 org", app1NocDate)))
                    .build())
            .respondent1OrganisationPolicy(
                caseData.getApplicant1OrganisationPolicy().toBuilder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("Res 1 org", res1NocDate)))
                    .build())
            .respondent2OrganisationPolicy(
                caseData.getApplicant1OrganisationPolicy().toBuilder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("Res 2 org", res2NocDate)))
                    .build())
            .build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        assertThat(roboticsCaseData.getNoticeOfChange()).isNull();
    }

    private PreviousOrganisationCollectionItem buildPreviousOrganisation(String name, LocalDateTime toDate) {
        return PreviousOrganisationCollectionItem.builder().value(
            PreviousOrganisation.builder().organisationName(name).toTimestamp(toDate).build()).build();
    }
}
