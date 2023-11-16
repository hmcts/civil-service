package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.ORGANISATION;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.GA_DOC_CATEGORY_ID;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_TRIAL_DATE_RANGE;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_UNAVAILABILITY_RANGE;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.TRIAL_DATE_FROM_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.UNAVAILABLE_DATE_RANGE_MISSING;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.UNAVAILABLE_FROM_MUST_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_CANNOT_BE_IN_PAST;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_SHOULD_NOT_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    InitiateGeneralApplicationService.class,
    JacksonAutoConfiguration.class,
    InitiateGeneralApplicationServiceHelper.class
})
class InitiateGeneralApplicationServiceTest extends LocationRefSampleDataBuilder {

    public static final String APPLICANT_EMAIL_ID_CONSTANT = "testUser@gmail.com";
    public static final String DEFENDANT_EMAIL_ID_CONSTANT = "testUser1@gmail.com";
    private static final String authToken = "Bearer TestAuthToken";
    private static final LocalDateTime weekdayDate = LocalDate.of(2022, 2, 15).atTime(12, 0);
    private static final Applicant1DQ applicant1DQ =
            Applicant1DQ.builder().applicant1DQRequestedCourt(RequestedCourt.builder()
                    .responseCourtCode("applicant1DQRequestedCourt")
                                                                  .caseLocation(CaseLocationCivil.builder()
                                                                                    .region("2")
                                                                                    .baseLocation("00000")
                                                                                    .build())
                                                                  .build()).build();
    private static final Respondent1DQ respondent1DQ =
            Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder()
                    .responseCourtCode("respondent1DQRequestedCourt")
                                                                    .caseLocation(CaseLocationCivil.builder()
                                                                                      .region("2")
                                                                                      .baseLocation("11111")
                                                                                      .build())
                                                                    .build()).build();
    private static final Respondent2DQ respondent2DQ =
        Respondent2DQ.builder().respondent2DQRequestedCourt(RequestedCourt.builder()
                                                                .responseCourtCode("respondent2DQRequestedCourt")
                                                                .caseLocation(CaseLocationCivil.builder()
                                                                                  .region("3")
                                                                                  .baseLocation("22222")
                                                                                  .build())
                                                                .build()).build();
    @Autowired
    private InitiateGeneralApplicationService service;

    @Autowired
    private InitiateGeneralApplicationServiceHelper helper;

    @MockBean
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    @MockBean
    private GeneralAppsDeadlinesCalculator calc;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private UserService userService;
    @MockBean
    private UserRoleCaching userRoleCaching;

    @MockBean
    private CrossAccessUserConfiguration crossAccessUserConfiguration;

    @MockBean
    private LocationRefDataService locationRefDataService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() throws IOException {
        when(calc.calculateApplicantResponseDeadline(
            any(LocalDateTime.class),
            anyInt()
        ))
            .thenReturn(weekdayDate);

        when(organisationApi.findUserOrganisation(any(), any()))
            .thenReturn(uk.gov.hmcts.reform.civil.prd.model.Organisation
                            .builder().organisationIdentifier("OrgId1").build());

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseAssignedApplicantUserRoles()).build());

        when(userService.getAccessToken(
            any(), any())).thenReturn(STRING_CONSTANT);

        when(helper.getCaaAccessToken()).thenReturn(STRING_CONSTANT);

        when(authTokenGenerator.generate()).thenReturn(STRING_CONSTANT);
    }

    @Nested
    class AboutToStart {
        private final String respondent1OrganizationID = "respondent1OrganizationID";
        private final String respondent1OrgPolicyReference = "respondentOrgPolicyReference";
        private final String respondent2OrganizationID = "respondent1OrganizationID";
        private final String respondent2OrgPolicyReference = "respondentOrgPolicyReference";

        private final OrganisationPolicy respondent1Organization = OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                        .organisationID(respondent1OrganizationID).build())
                .orgPolicyReference(respondent1OrgPolicyReference)
                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName())
                .build();
        private final OrganisationPolicy respondent2Organization = OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                        .organisationID(respondent2OrganizationID).build())
                .orgPolicyReference(respondent2OrgPolicyReference)
                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName())
                .build();

        /* 1V1 scenarios */
        @Test
        void shouldReturnTrue_whenRespondent1SolIsAssigned_1V1() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .build();
            List<String> userRoles = new ArrayList<>(Arrays.asList("[APPLICANTSOLICITORONE]", "[RESPONDENTSOLICITORONE]"));
            when(userRoleCaching.getUserRoles(any(), any())).thenReturn(userRoles);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(applicant1Respondent1SolAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenRespondentSolIsNotAssigned_1V1() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .build();
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(onlyApplicantSolicitorAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isFalse();
        }

        /* 1V2 Same defendant org scenarios */
        @Test
        void shouldReturnTrue_whenR1SolicitorsIsAssigned_1V2_SAME() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent2SameLegalRepresentative(YES)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .respondent2OrganisationPolicy(respondent2Organization)
                    .build();
            List<String> userRoles = new ArrayList<>(Arrays.asList("[APPLICANTSOLICITORONE]", "[RESPONDENTSOLICITORONE]"));
            when(userRoleCaching.getUserRoles(any(), any())).thenReturn(userRoles);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(applicant1Respondent1SolAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenR1AndR2AreAssigned_1V2_SAME() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent2SameLegalRepresentative(YES)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .respondent2OrganisationPolicy(respondent2Organization)
                    .build();
            List<String> userRoles = new ArrayList<>(Arrays.asList("[APPLICANTSOLICITORONE]", "[RESPONDENTSOLICITORONE]", "[RESPONDENTSOLICITORTWO]"));
            when(userRoleCaching.getUserRoles(any(), any())).thenReturn(userRoles);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(applicant1Respondent1Respondent2SolAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenRespondent1SolOnlyIsAssigned_1V2_SAME() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent2SameLegalRepresentative(YES)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .respondent2OrganisationPolicy(respondent2Organization)
                    .build();
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(applicant1Respondent2SolAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isFalse();
        }

        @Test
        void shouldReturnTrue_whenRespondent1SolOnlyIsAssigned_1V2_SAME() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent2SameLegalRepresentative(YES)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .respondent2OrganisationPolicy(respondent2Organization)
                    .build();
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(onlyApplicantSolicitorAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isFalse();
        }

        /* 1V2 Different defendant org scenarios */

        @Test
        void shouldReturnTrue_whenR1R2SolsAreAssigned_1V2_DIFF() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .respondent2OrganisationPolicy(respondent2Organization)
                    .build();
            List<String> userRoles = new ArrayList<>(Arrays.asList("[APPLICANTSOLICITORONE]", "[RESPONDENTSOLICITORONE]", "[RESPONDENTSOLICITORTWO]"));
            when(userRoleCaching.getUserRoles(any(), any())).thenReturn(userRoles);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(applicant1Respondent1Respondent2SolAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenR1R2SolsAreNotAssigned_1V2_DIFF() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .respondent2OrganisationPolicy(respondent2Organization)
                    .build();
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(onlyApplicantSolicitorAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenR1AssignedButR2NotAssigned_1V2_DIFF() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .respondent2OrganisationPolicy(respondent2Organization)
                    .build();
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(applicant1Respondent1SolAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenR2AssignedButR1NotAssigned_1V2_DIFF() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseReference(1L)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1OrganisationPolicy(respondent1Organization)
                    .respondent2OrganisationPolicy(respondent2Organization)
                    .build();
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(applicant1Respondent2SolAssigned()).build());

            assertThat(service.respondentAssigned(caseData, authToken)).isFalse();
        }
    }

    public List<CaseAssignedUserRole> onlyApplicantSolicitorAssigned() {
        return List.of(
            getCaseAssignedUserRole("org1Sol1", APPLICANTSOLICITORONE)
        );
    }

    public List<CaseAssignedUserRole> applicant1Respondent1SolAssigned() {
        return List.of(
            getCaseAssignedUserRole("org1Sol1", APPLICANTSOLICITORONE),
            getCaseAssignedUserRole("org2Sol1", RESPONDENTSOLICITORONE)
        );
    }

    public List<CaseAssignedUserRole> applicant1Respondent2SolAssigned() {
        return List.of(
            getCaseAssignedUserRole("org1Sol1", APPLICANTSOLICITORONE),
            getCaseAssignedUserRole("org3Sol1", RESPONDENTSOLICITORTWO)
        );
    }

    public List<CaseAssignedUserRole> applicant1Respondent1Respondent2SolAssigned() {
        return List.of(
            getCaseAssignedUserRole("org1Sol1", APPLICANTSOLICITORONE),
            getCaseAssignedUserRole("org2Sol1", RESPONDENTSOLICITORONE),
            getCaseAssignedUserRole("org3Sol1", RESPONDENTSOLICITORTWO)
        );
    }

    public CaseAssignedUserRole getCaseAssignedUserRole(String userId, CaseRole caseRole) {
        return CaseAssignedUserRole.builder().caseDataId("1").userId(userId)
            .caseRole(caseRole.getFormattedName()).build();
    }

    public List<CaseAssignedUserRole> getCaseAssignedApplicantUserRoles() {
        return List.of(
            CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("3")
                .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("4")
                .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("5")
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build()
        );
    }

    @Test
    void shouldReturnCaseDataPopulated_whenValidApplicationIsBeingInitiated() {

        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithEmptyCollectionOfApps(CaseData.builder().build());
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertCollectionPopulated(result);
        assertCaseDateEntries(result);
        result.getGeneralApplications().forEach(generalApplicationElement -> {
            assertCaseManagementCategoryPopulated(generalApplicationElement.getValue().getCaseManagementCategory());
        });
    }

    @Test
    void shouldReturnCaseDataWithAdditionToCollection_whenAnotherApplicationIsBeingInitiated() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataCollectionOfApps(CaseData.builder().build());
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(2);
    }

    @Test
    void shouldNotPopulateInformOtherPartyAndStatementOfTruthIfConsentInfoNotProvided() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataForConsentUnconsentCheck(null);
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(1);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppInformOtherParty().getIsWithNotice())
            .isNull();
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppInformOtherParty()
                       .getReasonsForWithoutNotice()).isNull();
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppStatementOfTruth().getName())
            .isNull();
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppStatementOfTruth().getRole())
            .isNull();
    }

    @Test
    void shouldPopulateStatementOfTruthAndSetNoticeAndConsentOrderIfConsented() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataForConsentUnconsentCheck(GARespondentOrderAgreement.builder().hasAgreed(YES).build());
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(1);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppInformOtherParty().getIsWithNotice())
            .isEqualTo(YES);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppInformOtherParty()
                       .getReasonsForWithoutNotice()).isNull();
        assertThat(result.getGeneralApplications().get(0).getValue()
                .getGeneralAppConsentOrder()).isNotNull();
        assertThat(result.getGeneralApplications().get(0).getValue()
                .getGeneralAppConsentOrder()).isEqualTo(NO);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppStatementOfTruth().getName())
            .isNotNull();
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppStatementOfTruth().getRole())
            .isNotNull();
    }

    @Test
    void shouldNotPopulateStatementOfTruthAndSetNoticeAndConsentOrderIfConsented() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataForStatementOfTruthCheck(GARespondentOrderAgreement.builder().hasAgreed(YES).build());

        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(1);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppInformOtherParty().getIsWithNotice())
            .isEqualTo(YES);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppInformOtherParty()
                       .getReasonsForWithoutNotice()).isNull();
        assertThat(result.getGeneralApplications().get(0).getValue()
                       .getGeneralAppConsentOrder()).isNotNull();
        assertThat(result.getGeneralApplications().get(0).getValue()
                       .getGeneralAppConsentOrder()).isEqualTo(NO);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppStatementOfTruth().getName())
            .isNull();
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppStatementOfTruth().getRole())
            .isNull();
    }

    @Test
    void shoulAddSpecClaimType() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataSPEC(SPEC_CLAIM);
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(1);
        assertThat(result.getGeneralApplications().get(0).getValue()
                       .getGeneralAppSuperClaimType()).isEqualTo("SPEC_CLAIM");
    }

    @Test
    void shoulAddUnSpecClaimType() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataSPEC(null);
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(1);
        assertThat(result.getGeneralApplications().get(0).getValue()
                       .getGeneralAppSuperClaimType()).isEqualTo("UNSPEC_CLAIM");
    }

    @Test
    void shouldPopulateInformOtherPartyAndStatementOfTruthIfUnconsented() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataForConsentUnconsentCheck(GARespondentOrderAgreement.builder().hasAgreed(NO).build());
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(1);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppInformOtherParty().getIsWithNotice())
            .isEqualTo(NO);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppInformOtherParty()
                       .getReasonsForWithoutNotice()).isEqualTo(STRING_CONSTANT);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppStatementOfTruth().getName())
            .isEqualTo(STRING_CONSTANT);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppStatementOfTruth().getRole())
            .isEqualTo(STRING_CONSTANT);
    }

    //Urgency Date validation
    @Test
    void shouldReturnErrors_whenApplicationIsUrgentButConsiderationDateIsNotProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(YES)
            .urgentAppConsiderationDate(null)
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(URGENCY_DATE_REQUIRED);
    }

    @Test
    void shouldReturnErrors_whenApplicationIsNotUrgentButConsiderationDateIsProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(NO)
            .urgentAppConsiderationDate(LocalDate.now())
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(URGENCY_DATE_SHOULD_NOT_BE_PROVIDED);
    }

    @Test
    void shouldReturnErrors_whenUrgencyConsiderationDateIsInPastForUrgentApplication() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(YES)
            .urgentAppConsiderationDate(LocalDate.now().minusDays(1))
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(URGENCY_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldNotCauseAnyErrors_whenUrgencyConsiderationDateIsInFutureForUrgentApplication() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(YES)
            .urgentAppConsiderationDate(LocalDate.now())
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(NO)
            .urgentAppConsiderationDate(null)
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isEmpty();
    }

    //Trial Dates validations
    @Test
    void shouldReturnErrors_whenTrialIsScheduledButTrialDateFromIsNull() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(null)
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(TRIAL_DATE_FROM_REQUIRED);
    }

    @Test
    void shouldReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToBeforeIt() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(LocalDate.now().minusDays(1))
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(INVALID_TRIAL_DATE_RANGE);
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithNullTrialDateTo() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToAfterIt() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(LocalDate.now().plusDays(1))
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedAndTrialDateToAreSame() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(LocalDate.now())
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsNotScheduled() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(NO)
            .trialDateFrom(null)
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    //Unavailability Dates validations
    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButNullDateRangeProvided() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(null)
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(UNAVAILABLE_DATE_RANGE_MISSING);
    }

    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasNullDateFrom() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(null)
            .unavailableTrialDateTo(null)
            .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
    }

    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasDateFromAfterDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now().plusDays(1))
            .unavailableTrialDateTo(LocalDate.now())
            .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(INVALID_UNAVAILABILITY_RANGE);
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsNotSet() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(NO)
            .trialDateFrom(null)
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(NO)
            .generalAppUnavailableDates(null)
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithNullDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now())
            .unavailableTrialDateTo(null)
            .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(NO)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithSameDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now())
            .unavailableTrialDateTo(LocalDate.now())
            .build();
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(NO)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsBeforeDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now())
            .unavailableTrialDateTo(LocalDate.now().plusDays(1))
            .build();
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(NO)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnDate_whenGeneralAppNotificationDeadlineIsInvoked() {
        LocalDateTime givenDate = GeneralApplication.builder()
            .generalAppDateDeadline(weekdayDate)
            .build()
            .getGeneralAppDateDeadline();

        String actual = "2022-02-15T12:00";

        assertThat(givenDate).isEqualTo(actual).isNotNull();
    }

    @Test
    void shouldPopulatePartyNameDetails() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataForConsentUnconsentCheck(GARespondentOrderAgreement.builder().hasAgreed(NO).build());
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(1);
        assertThat(result.getGeneralApplications().get(0).getValue().getClaimant1PartyName()).isEqualTo("Applicant1");
        assertThat(result.getGeneralApplications().get(0).getValue().getClaimant2PartyName()).isEqualTo("Applicant2");
        assertThat(result.getGeneralApplications().get(0).getValue().getDefendant1PartyName()).isEqualTo("Respondent1");
        assertThat(result.getGeneralApplications().get(0).getValue().getDefendant2PartyName()).isEqualTo("Respondent2");

    }

    @Test
    void shouldReturnTrue_whenApplicantIsClaimantAtMainCase() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseData(CaseDataBuilder.builder().build());
        List<String> userRoles = new ArrayList<>(Arrays.asList("[APPLICANTSOLICITORONE]"));
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        builder.applicant1OrganisationPolicy(OrganisationPolicy
                                                 .builder().orgPolicyCaseAssignedRole("[APPLICANTSOLICITORONE]").build());
        when(userRoleCaching.getUserRoles(any(), any())).thenReturn(userRoles);
        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(onlyApplicantSolicitorAssigned()).build());

        boolean result = service.isGAApplicantSameAsParentCaseClaimant(builder.build(), authToken);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenApplicantIsClaimantAtMainCase() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseData(CaseDataBuilder.builder().build());
        List<String> userRoles = new ArrayList<>(Arrays.asList("[APPLICANTSOLICITORONE]", "[RESPONDENTSOLICITORTWO]"));

        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        builder.applicant1OrganisationPolicy(OrganisationPolicy
                                                 .builder().orgPolicyCaseAssignedRole("[APPLICANTSOLICITORONE]").build());
        when(userRoleCaching.getUserRoles(any(), any())).thenReturn(userRoles);
        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(applicant1Respondent2SolAssigned()).build());

        boolean result = service.isGAApplicantSameAsParentCaseClaimant(builder.build(), authToken);

        assertFalse(result);
    }

    @Test
    void shouldPopulateApplicantDetails() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataForConsentUnconsentCheck(GARespondentOrderAgreement.builder().hasAgreed(NO).build());
        when(locationRefDataService.getCtscLocation(any(), any()))
            .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").siteName("CTSC")
                            .courtAddress("Prince William House, Peel Cross Road, Salford").postcode("M5 4RR").build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).id(STRING_NUM_CONSTANT).build(), CallbackParams.builder().toString());

        assertThat(result.getGeneralApplications().size()).isEqualTo(1);
        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppApplnSolicitor().getId())
                .isEqualTo(STRING_NUM_CONSTANT);

        assertThat(result.getGeneralApplications().get(0).getValue()
                       .getGeneralAppRespondentSolicitors().size()).isEqualTo(4);

        assertThat(result.getGeneralApplications().get(0).getValue().getLocationName())
            .isEqualTo("CTSC");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getAddress())
            .isEqualTo("Prince William House, Peel Cross Road, Salford");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getPostcode())
            .isEqualTo("M5 4RR");

        assertThat(result.getGeneralApplications().get(0).getValue().getGeneralAppRespondentSolicitors()
                       .stream().filter(e -> STRING_NUM_CONSTANT.equals(e.getValue().getId())).count()).isEqualTo(0);
    }

    @Test
    void shouldPopulateLocationDetailsForBaseLocation() {
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(getSampleCourLocationsRefObject());
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(null, null, INDIVIDUAL, applicant1DQ, respondent1DQ,
                                          respondent2DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("11111");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getSiteName())
            .isEqualTo("locationOfRegion2");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getAddress())
            .isEqualTo("Prince William House, Peel Cross Road, Salford");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getPostcode())
            .isEqualTo("M5 4RR");
    }

    protected List<LocationRefData> getSampleCourLocationsRefObject() {
        return new ArrayList<>(List.of(
            LocationRefData.builder()
                .epimmsId("11111").siteName("locationOfRegion2").courtAddress("Prince William House, Peel Cross Road, Salford")
                .postcode("M5 4RR")
                .courtLocationCode("court1").build()
        ));
    }

    protected List<LocationRefData> getEmptyCourLocationsRefObject() {
        return new ArrayList<>(List.of(
            LocationRefData.builder().build()
        ));
    }

    protected List<LocationRefData> getSampleCourtLocationsRefObjectMultipleValues() {
        return new ArrayList<>(List.of(
            LocationRefData.builder()
                .epimmsId("11111").siteName("locationOfRegion2").courtAddress("Prince William House, Peel Cross Road, Salford")
                .postcode("M5 4RR")
                .courtLocationCode("court1").build(),

            LocationRefData.builder()
                .epimmsId("11111").siteName("locationOfRegion2").courtAddress("Prince William House, Peel Cross Road, Salford")
                .postcode("M5 4RR")
                .courtLocationCode("court1").build()
        ));
    }

    @Test
    void shouldPopulateLocationDetailsForBaseLocationWhereListIsEmpty() {
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(getEmptyCourLocationsRefObject());
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(null, null, INDIVIDUAL, applicant1DQ, respondent1DQ,
                                          respondent2DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("11111");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getSiteName())
            .isNull();
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getAddress())
            .isNull();
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getPostcode())
            .isNull();
    }

    @Test
    void shouldPopulateLocationDetailsForBaseLocationWithMultipleListValues() {
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(getSampleCourtLocationsRefObjectMultipleValues());
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(null, null, INDIVIDUAL, applicant1DQ, respondent1DQ,
                                          respondent2DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("11111");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getSiteName())
            .isEqualTo("locationOfRegion2");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getAddress())
            .isEqualTo("Prince William House, Peel Cross Road, Salford");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getPostcode())
            .isEqualTo("M5 4RR");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_beforeSDOHasBeenMade() {
        when(locationRefDataService.getCtscLocation(any(), any()))
                .thenReturn(LocationRefData.builder().regionId("9").epimmsId("574546").build());
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(CASE_ISSUED, null, INDIVIDUAL, applicant1DQ, respondent1DQ,
                                              respondent2DQ);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("574546");
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getRegion())
                .isEqualTo("9");
        assertThat(result.getGeneralApplications().get(0).getValue().getIsCtscLocation()).isEqualTo(YES);
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementCategory().getValue().getLabel())
                .isEqualTo("Civil");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMadeForUnspecIndividualResp() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(null, null, INDIVIDUAL, applicant1DQ, respondent1DQ,
                                             respondent2DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("11111");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMadeForSpecIndividualClaimant() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(null, SPEC_CLAIM, INDIVIDUAL, applicant1DQ, respondent1DQ,
                                              Respondent2DQ.builder().build());
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("11111");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMade1V1IndividualResp() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation1V1(null, SPEC_CLAIM, INDIVIDUAL, applicant1DQ, respondent1DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("11111");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMade1V1CompanyResp() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation1V1(null, SPEC_CLAIM, COMPANY, applicant1DQ, respondent1DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("00000");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMade1V1SoleTraderDefType() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation1V1(null, SPEC_CLAIM, SOLE_TRADER, applicant1DQ, respondent1DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("11111");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMade1V1OrgDefType() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation1V1(null, SPEC_CLAIM, ORGANISATION, applicant1DQ, respondent1DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("00000");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMadeForSpecSoleTraderClaimant() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(null, SPEC_CLAIM, SOLE_TRADER, applicant1DQ, respondent1DQ,
                                               respondent2DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("11111");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMadeForSpecCompanyClaimant() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(null, SPEC_CLAIM, COMPANY, applicant1DQ, respondent1DQ,
                                              respondent2DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("00000");
    }

    @Test
    void shouldPopulateWorkAllocationLocationOnAboutToSubmit_afterSDOHasBeenMadeForSpecOrgClaimant() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(null, SPEC_CLAIM, ORGANISATION, applicant1DQ, respondent1DQ,
                                              respondent2DQ);
        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("00000");
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeForSpecIndClaimantWithoutCourtDetails1() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(null, SPEC_CLAIM, INDIVIDUAL, null, respondent1DQ,
                                              respondent2DQ);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("11111");
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeForSpecIndClaimantWithRespondent1FirstResponded() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getMultiCaseDataForWorkAllocationForOne_V_Two(null, SPEC_CLAIM, INDIVIDUAL,
                        Applicant1DQ.builder().build(), respondent1DQ, respondent2DQ);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("22222");
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeForSpecIndClaimantWithoutCourtDetails3() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(null, SPEC_CLAIM, INDIVIDUAL, Applicant1DQ.builder()
                        .applicant1DQRequestedCourt(RequestedCourt.builder().build()).build(), respondent1DQ,
                                              respondent2DQ);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("11111");
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeWhenRespondentTypeIsOrg() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getCaseDataForWorkAllocation(null, SPEC_CLAIM, ORGANISATION, applicant1DQ, null,
                                              null);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("00000");
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeWhenRespondentTypeIsIndividual() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(null, SPEC_CLAIM, INDIVIDUAL, applicant1DQ, null,
                                          null);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isNull();
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeWhenRespondentTypeIsCompany() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(null, SPEC_CLAIM, ORGANISATION, Applicant1DQ.builder()
                                              .applicant1DQRequestedCourt(RequestedCourt.builder().build()).build(),
                                          null,
                                          null);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("00000");
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeForMultiClaimantWithCourtDetails() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(null, SPEC_CLAIM, ORGANISATION, applicant1DQ,
                                          Respondent1DQ.builder().build(), Respondent2DQ.builder().build());

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("00000");
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeForMultiDefendantWithRespondent1RespondedFirst() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(null, SPEC_CLAIM, INDIVIDUAL, applicant1DQ,
                                          respondent1DQ, respondent2DQ);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
            .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
            .isEqualTo("11111");
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOHasBeenMadeForOne_V_TwoWhereSecondDefRespondedFirst() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getMultiCaseDataForWorkAllocationForOne_V_Two(null, SPEC_CLAIM, SOLE_TRADER, applicant1DQ,
                        Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder().build()).build(),
                        respondent2DQ);

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0).getValue().getCaseManagementLocation().getBaseLocation())
                .isEqualTo("22222");
    }

    @Test
    void shouldCopyN245toEvidenceWithCategoryId_whenCreateVaryApplication() {
        CaseData caseData = new GeneralApplicationDetailsBuilder()
                .getVaryJudgmentWithN245TestData();

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData, UserDetails.builder()
                .email(APPLICANT_EMAIL_ID_CONSTANT).build(), CallbackParams.builder().toString());
        assertThat(result.getGeneralApplications().get(0)
                .getValue().getGeneralAppEvidenceDocument()).hasSize(2);
        assertThat(result.getGeneralApplications().get(0)
                .getValue().getGeneralAppEvidenceDocument().get(1).getValue().getCategoryID())
                .isEqualTo(GA_DOC_CATEGORY_ID);
    }

    private void assertCaseDateEntries(CaseData caseData) {
        assertThat(caseData.getGeneralAppType().getTypes()).isNull();
        assertThat(caseData.getGeneralAppRespondentAgreement().getHasAgreed()).isNull();
        assertThat(caseData.getGeneralAppDetailsOfOrder()).isEmpty();
        assertThat(caseData.getGeneralAppReasonsOfOrder()).isEmpty();
        assertThat(caseData.getGeneralAppInformOtherParty().getIsWithNotice()).isNull();
        assertThat(caseData.getGeneralAppInformOtherParty().getReasonsForWithoutNotice()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getReasonsForUrgency()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate()).isNull();
        assertThat(caseData.getGeneralAppStatementOfTruth().getName()).isNull();
        assertThat(caseData.getGeneralAppStatementOfTruth().getRole()).isNull();
        assertThat(unwrapElements(caseData.getGeneralAppEvidenceDocument())).isEmpty();
        GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails();
        assertThat(generalAppHearingDetails.getJudgeName()).isNull();
        assertThat(generalAppHearingDetails.getHearingDate()).isNull();
        assertThat(generalAppHearingDetails.getTrialDateFrom()).isNull();
        assertThat(generalAppHearingDetails.getTrialDateTo()).isNull();
        assertThat(generalAppHearingDetails.getHearingYesorNo()).isNull();
        assertThat(generalAppHearingDetails.getHearingDuration()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirement()).isNull();
        assertThat(generalAppHearingDetails.getJudgeRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getTrialRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getHearingDetailsEmailID()).isNull();
        assertThat(generalAppHearingDetails.getGeneralAppUnavailableDates()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementOther()).isNull();
        assertThat(generalAppHearingDetails.getHearingDetailsTelephoneNumber()).isNull();
        assertThat(generalAppHearingDetails.getReasonForPreferredHearingType()).isNull();
        assertThat(generalAppHearingDetails.getTelephoneHearingPreferredType()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementSignLanguage()).isNull();
        assertThat(generalAppHearingDetails.getHearingPreferencesPreferredType()).isNull();
        assertThat(generalAppHearingDetails.getUnavailableTrialRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementLanguageInterpreter()).isNull();
    }

    private void assertCaseManagementCategoryPopulated(GACaseManagementCategory gaCaseManagementCategory) {
        assertThat(gaCaseManagementCategory.getValue().getCode())
            .isEqualTo(CASE_MANAGEMENT_CATEGORY);
        assertThat(gaCaseManagementCategory.getValue().getLabel())
            .isEqualTo(CASE_MANAGEMENT_CATEGORY);
        assertThat(gaCaseManagementCategory.getList_items().get(0).getValue().getLabel())
            .isEqualTo(CASE_MANAGEMENT_CATEGORY);
        assertThat(gaCaseManagementCategory.getList_items().get(0).getValue().getCode())
            .isEqualTo(CASE_MANAGEMENT_CATEGORY);
    }

    private void assertCollectionPopulated(CaseData caseData) {
        assertThat(unwrapElements(caseData.getGeneralApplications()).size()).isEqualTo(1);
        GeneralApplication application = unwrapElements(caseData.getGeneralApplications()).get(0);

        assertThat(application.getGeneralAppType().getTypes().contains(EXTEND_TIME)).isTrue();
        assertThat(application.getGeneralAppRespondentAgreement().getHasAgreed()).isEqualTo(NO);
        assertThat(application.getGeneralAppDetailsOfOrder()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppReasonsOfOrder()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppInformOtherParty().getIsWithNotice())
            .isEqualTo(NO);
        assertThat(application.getGeneralAppInformOtherParty().getReasonsForWithoutNotice())
            .isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppUrgencyRequirement().getGeneralAppUrgency())
            .isEqualTo(YES);
        assertThat(application.getGeneralAppUrgencyRequirement().getReasonsForUrgency())
            .isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate())
            .isEqualTo(APP_DATE_EPOCH);
        assertThat(application.getGeneralAppStatementOfTruth().getName()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppStatementOfTruth().getRole()).isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentUrl())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentHash())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentBinaryUrl())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentFileName())
            .isEqualTo(STRING_CONSTANT);
        GAHearingDetails generalAppHearingDetails = application.getGeneralAppHearingDetails();
        assertThat(generalAppHearingDetails.getJudgeName()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingDate()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getTrialDateFrom()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getTrialDateTo()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getHearingYesorNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getHearingDuration()).isEqualTo(OTHER);
        assertThat(generalAppHearingDetails.getGeneralAppHearingDays()).isEqualTo("1");
        assertThat(generalAppHearingDetails.getGeneralAppHearingHours()).isEqualTo("2");
        assertThat(generalAppHearingDetails.getGeneralAppHearingMinutes()).isEqualTo("30");
        assertThat(generalAppHearingDetails.getSupportRequirement()
                       .contains(OTHER_SUPPORT)).isTrue();
        assertThat(generalAppHearingDetails.getJudgeRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getTrialRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getHearingDetailsEmailID()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getGeneralAppUnavailableDates().get(0).getValue()
                       .getUnavailableTrialDateFrom()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getGeneralAppUnavailableDates().get(0).getValue()
                       .getUnavailableTrialDateTo()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getSupportRequirementOther()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingDetailsTelephoneNumber())
            .isEqualTo(STRING_NUM_CONSTANT);
        assertThat(generalAppHearingDetails.getReasonForPreferredHearingType()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getTelephoneHearingPreferredType()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getSupportRequirementSignLanguage()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingPreferencesPreferredType())
            .isEqualTo(IN_PERSON);
        assertThat(generalAppHearingDetails.getUnavailableTrialRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getSupportRequirementLanguageInterpreter()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getIsMultiParty()).isEqualTo(YES);
    }
}

