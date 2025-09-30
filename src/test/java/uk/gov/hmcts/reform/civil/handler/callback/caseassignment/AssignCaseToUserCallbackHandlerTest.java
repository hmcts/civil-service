package uk.gov.hmcts.reform.civil.handler.callback.caseassignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment.AssignCaseToUserCallbackHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.AssignCaseToResopondentSolHelper;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.roleassignment.RolesAndAccessAssignmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SpringBootTest(classes = {
    AssignCaseToUserCallbackHandler.class,
    AssignCaseToResopondentSolHelper.class,
    GaForLipService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class AssignCaseToUserCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AssignCaseToUserCallbackHandler assignCaseToUserHandler;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private GeneralAppFeesService generalAppFeesService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private RolesAndAccessAssignmentService rolesAndAccessAssignmentService;

    private CallbackParams params;
    private GeneralApplication generalApplication;
    private GeneralApplication generalApplicationWithNotice;

    public static final Long CASE_ID = 1594901956117591L;
    public static final int RESPONDENT_ONE = 0;
    public static final int RESPONDENT_TWO = 1;
    public static final String SPEC_CLAIM = "SPEC_CLAIM";
    public static final String UNSPEC_CLAIM = "UNSPEC_CLAIM";
    public static final String STRING_NUM_CONSTANT = "this is a string";

    @Nested
    class AssignRolesUnspecCase {
        @BeforeEach
        void setup() {
            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .defendant1PartyName("Respondent1")
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .generalAppSuperClaimType(UNSPEC_CLAIM)
                    .isMultiParty(YES)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();

            Map<String, Object> dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldCallAssignCase_3Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(3)).assignCase(
                    any(),
                    any(),
                    any(),
                    any()
            );
        }

        @Test
        void shouldThrowExceptionIfSolicitorsAreNull() {
            Exception exception = assertThrows(Exception.class, () -> {
                assignCaseToUserHandler.handle(getCaseDateWithNoSolicitor(SPEC_CLAIM));
            });
            String expectedMessage = "java.lang.NullPointerException";
            String actualMessage = exception.toString();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    private List<CaseAssignmentUserRole> getCaseAssignedApplicantUserRoles() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("3")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("4")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("5")
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build()
        );
    }

    @Nested
    class AssignRolesUnspecCaseWithOutNoticeApplication {
        @BeforeEach
        void setup() {

            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
            List<Element<GASolicitorDetailsGAspec>> addlApplicantSol = new ArrayList<>();
            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec addlApplicant1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org1").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            addlApplicantSol.add(element(addlApplicant1));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .generalAppApplicantAddlSolicitors(addlApplicantSol)
                    .defendant1PartyName("Respondent1")
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .isMultiParty(YesOrNo.NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
                    .generalAppSuperClaimType(UNSPEC_CLAIM)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();

            Map<String, Object> dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
            params = callbackParamsOfPendingState(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOneAndTwoUnspec() {
            assignCaseToUserHandler.handle(params);
            verifyApplicantSolicitorOneRoles();
        }

        @Test
        void shouldCallAssignCase_2Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(2)).assignCase(
                    any(),
                    any(),
                    any(),
                    any()
            );
        }
    }

    @Nested
    class AssignDefendantRoleForGALip {
        @BeforeEach
        void setup() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").build();

            respondentSols.add(element(respondent1));
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .isGaRespondentOneLip(YES)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .isMultiParty(YesOrNo.NO)
                    .defendant1PartyName("Respondent1")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();
            Map<String, Object> data = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });

            params = callbackParamsOf(data, CallbackType.SUBMITTED);
        }

        @Test
        public void shouldAssignDefendantRoleToRespondent() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(1)).assignCase(any(), any(), any(), any());
        }

        @Test
        public void shouldCallAssignCaseWithDefendantRole() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(1))
                    .assignCase(CASE_ID.toString(),
                            generalApplication
                                    .getGeneralAppRespondentSolicitors().get(0).getValue().getId(), null,
                            CaseRole.DEFENDANT
                );
        }
    }

    @Nested
    class AssignRolesSpecCase {
        @BeforeEach
        void setup() {
            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .isMultiParty(YES)
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
                    .defendant1PartyName("Respondent1")
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();

            Map<String, Object> dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldCallAssignCase_3Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(3)).assignCase(
                    any(),
                    any(),
                    any(),
                    any()
            );
        }
    }

    @Nested
    class AssignRolesSpecCaseWithoutNotice {
        @BeforeEach
        void setup() {
            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .isMultiParty(YES)
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
                    .defendant1PartyName("Respondent1")
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();

            Map<String, Object> dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldNotCallAssignCase() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(0)).assignCase(
                    any(),
                    any(),
                    any(),
                    any()
            );
        }
    }

    @Nested
    class AssignRolesSpecCaseLipResp {
        @BeforeEach
        void setup() {
            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(ADJOURN_HEARING))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .isMultiParty(NO)
                    .isGaRespondentOneLip(YES)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.YES).build())
                    .defendant1PartyName("Respondent1")
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();

            Map<String, Object> dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldNotCallAssignCase() {
            var response = (AboutToStartOrSubmitCallbackResponse) assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(0)).assignCase(
                    any(),
                    any(),
                    any(),
                    any()
            );
        }

        @Test
        void shouldHaveDefendantRole() {
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) assignCaseToUserHandler.handle(params);
            assertThat(response.getData().get("respondent1OrganisationPolicy"))
                    .extracting("OrgPolicyCaseAssignedRole").isEqualTo("[DEFENDANT]");
        }
    }

    @Nested
    class AssignRolesSpecCaseLipApp {
        @BeforeEach
        void setup() {
            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            respondentSols.add(element(respondent1));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(ADJOURN_HEARING))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .build())
                    .isMultiParty(NO)
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(YES)
                    .isGaRespondentTwoLip(NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.YES).build())
                    .defendant1PartyName("Respondent1")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();

            Map<String, Object> dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
            params = callbackParamsOfPendingState(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldCallAssignCaseLip() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(1)).assignCase(
                    any(),
                    any(),
                    any(),
                    eq(CLAIMANT)
            );
        }
    }

    @Nested
    class AssignRolesSpecCaseWithNotice {
        @BeforeEach
        void setup() {
            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .isMultiParty(YES)
                    .defendant1PartyName("Respondent1")
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();

            Map<String, Object> dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldCallAssignCase_3Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(3)).assignCase(
                    any(),
                    any(),
                    any(),
                    any()
            );
        }
    }

    @Nested
    class AssignRolesSpecCaseForWithoutNoticeApplication {
        @BeforeEach
        void setup() {
            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id3")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id2")
                    .email("test@gmail.com").organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent3 = GASolicitorDetailsGAspec.builder().id("id1")
                    .email("test@gmail.com").organisationIdentifier("org2").build();
            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            respondentSols.add(element(respondent3));

            GASolicitorDetailsGAspec addlApplicant = GASolicitorDetailsGAspec.builder().id("id1")
                    .email("test@gmail.com").organisationIdentifier("Org1").build();
            List<Element<GASolicitorDetailsGAspec>> addlApplSols = new ArrayList<>();
            addlApplSols.add(element(addlApplicant));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder.generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .generalAppApplicantAddlSolicitors(addlApplSols)
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .defendant1PartyName("Respondent1")
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .isMultiParty(YesOrNo.NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            generalApplication = builder.build();

            Map<String, Object> dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
            params = callbackParamsOfPendingState(dataMap, CallbackType.SUBMITTED);
        }

        public List<CaseAssignmentUserRole> getCaseAssignedApplicantUserRoles() {

            return List.of(
                    CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                            .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                    CaseAssignmentUserRole.builder().caseDataId("1").userId("2")
                            .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                    CaseAssignmentUserRole.builder().caseDataId("1").userId("3")
                            .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                    CaseAssignmentUserRole.builder().caseDataId("1").userId("4")
                            .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                    CaseAssignmentUserRole.builder().caseDataId("1").userId("5")
                            .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build()
            );
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOneAndTwoUnspec() {
            assignCaseToUserHandler.handle(params);
            verifyApplicantSolicitorOneSpecRoles();
        }

        @Test
        void shouldCallAssignCase_1Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(2)).assignCase(
                    any(),
                    any(),
                    any(),
                    any()
            );
        }
    }

    @Nested
    class AssignRoles1V3 {

        Map<String, Object> dataMap;
        Map<String, Object> dataMapWithNotice;

        @BeforeEach
        void setup() {
            when(coreCaseUserService.getUserRoles(any()))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseAssignedApplicantUserRoles()).build());
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org3").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent3 = GASolicitorDetailsGAspec.builder().id("id")
                    .email("test@gmail.com").organisationIdentifier("org2").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            respondentSols.add(element(respondent3));

            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
            builder
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .isMultiParty(YES)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
                    .defendant1PartyName("Respondent1")
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder().id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .build();

            GeneralApplication.GeneralApplicationBuilder builderWithNotice = GeneralApplication.builder();
            builderWithNotice.generalAppType(GAApplicationType.builder()
                            .types(singletonList(SUMMARY_JUDGEMENT))
                            .build())
                    .claimant1PartyName("Applicant1")
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                            .builder()
                            .id("id")
                            .email("TEST@gmail.com")
                            .organisationIdentifier("Org1").build())
                    .isMultiParty(YES)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
                    .defendant1PartyName("Respondent1")
                    .claimant2PartyName("Applicant2")
                    .defendant2PartyName("Respondent2")
                    .generalAppSuperClaimType(SPEC_CLAIM)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                    .civilServiceUserRoles(IdamUserDetails.builder()
                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                            .email("applicant@someorg.com")
                            .build())
                    .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                    .isGaRespondentOneLip(NO)
                    .isGaApplicantLip(NO)
                    .isGaRespondentTwoLip(NO)
                    .build();

            generalApplicationWithNotice = builderWithNotice.build();
            generalApplication = builder.build();
            dataMapWithNotice = objectMapper.convertValue(generalApplicationWithNotice, new TypeReference<>() {
            });
            dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {
            });
        }

        @Test
        void shouldCallAssignCaseResp_4TimeAwaitingPayment() {
            params = callbackParamsOf(dataMapWithNotice, CallbackType.ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response
                    = (AboutToStartOrSubmitCallbackResponse)
                    assignCaseToUserHandler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCallAssignCaseApp_1TimePending() {
            params = callbackParamsOfPendingState(dataMap, CallbackType.ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response
                    = (AboutToStartOrSubmitCallbackResponse)
                    assignCaseToUserHandler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCallAssignCaseResp_4TimesNoPending() throws Exception {
            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

            // print out the raw CaseDetails data so we can inspect the keys
            System.out.println(
                    objectMapper.writeValueAsString(
                            params.getRequest().getCaseDetails().getData()
                    )
            );

            // resp1 sol1 has been assigned twice, one at line 36 then in for loop
            AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) assignCaseToUserHandler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCallAssignCaseRespSubmitted_4TimesNoPending() {
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
            //resp1 sol1 has been assigned twice, one at line 36 then in for loop
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(2)).assignCase(
                    any(),
                    any(),
                    any(),
                    eq(RESPONDENTSOLICITORONE)
            );
            verify(coreCaseUserService, times(2)).assignCase(
                    any(),
                    any(),
                    any(),
                    eq(RESPONDENTSOLICITORTWO)
            );
        }
    }

    private void verifyApplicantSolicitorOneRoles() {
        verify(coreCaseUserService).assignCase(
                CASE_ID.toString(),
                generalApplication.getGeneralAppApplnSolicitor().getId(),
                "Org1",
                CaseRole.APPLICANTSOLICITORONE
        );
    }

    private void verifyApplicantSolicitorOneSpecRoles() {
        verify(coreCaseUserService).assignCase(
                CASE_ID.toString(),
                generalApplication.getGeneralAppApplnSolicitor().getId(),
                "Org1",
                CaseRole.APPLICANTSOLICITORONE
        );
    }

    private void verifyRespondentSolicitorOneRoles() {
        verify(coreCaseUserService).assignCase(
                CASE_ID.toString(),
                generalApplication.getGeneralAppRespondentSolicitors()
                        .get(RESPONDENT_ONE).getValue().getId(),
                "org2",
                CaseRole.RESPONDENTSOLICITORONE
        );
    }

    private void verifyRespondentSolicitorTwoRoles() {
        verify(coreCaseUserService).assignCase(
                CASE_ID.toString(),
                generalApplication.getGeneralAppRespondentSolicitors()
                        .get(RESPONDENT_ONE).getValue().getId(),
                "org2",
                CaseRole.RESPONDENTSOLICITORTWO
        );
    }

    private void verifyRespondentSolicitorOneSpecRoles() {
        verify(coreCaseUserService).assignCase(
                CASE_ID.toString(),
                generalApplication.getGeneralAppRespondentSolicitors()
                        .get(RESPONDENT_TWO).getValue().getId(),
                "org2",
                CaseRole.RESPONDENTSOLICITORONE
        );
    }

    private void verifyRespondentSolicitorTwoSpecRoles() {
        verify(coreCaseUserService).assignCase(
                CASE_ID.toString(),
                generalApplication.getGeneralAppRespondentSolicitors()
                        .get(RESPONDENT_TWO).getValue().getId(),
                "org2",
                CaseRole.RESPONDENTSOLICITORTWO
        );
    }

    public CallbackParams getCaseDateWithNoSolicitor(String claimType) {

        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        builder.generalAppType(GAApplicationType.builder()
                        .types(singletonList(SUMMARY_JUDGEMENT))
                        .build())
                .claimant1PartyName("Applicant1")
                .defendant1PartyName("Respondent1")
                .claimant2PartyName("Applicant2")
                .defendant2PartyName("Respondent2")
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("12342341").build())
                .generalAppSuperClaimType(claimType)
                .civilServiceUserRoles(IdamUserDetails.builder()
                        .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                        .email("applicant@someorg.com")
                        .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

        GeneralApplication caseData = builder.build();

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        return callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
    }

}
