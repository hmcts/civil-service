package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.caseassignment;

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

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.service.AssignCaseToRespondentSolHelper;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.roleassignment.RolesAndAccessAssignmentService;
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
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class AssignCaseToUserCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private AssignCaseToUserCallbackHandler assignCaseToUserHandler;

    private AssignCaseToRespondentSolHelper assignCaseToRespondentSolHelper;

    @Mock private CoreCaseUserService coreCaseUserService;

    @Mock private GeneralAppFeesService generalAppFeesService;

    @Spy private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Mock private RolesAndAccessAssignmentService rolesAndAccessAssignmentService;

    @Mock private GaForLipService gaForLipService;

    @Spy private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    private CallbackParams params;
    private GeneralApplication generalApplication;
    private GeneralApplication generalApplicationWithNotice;

    public static final Long CASE_ID = 1594901956117591L;

    @BeforeEach
    void initHandler() {
        assignCaseToRespondentSolHelper =
                new AssignCaseToRespondentSolHelper(coreCaseUserService, gaForLipService);
        assignCaseToUserHandler =
                new AssignCaseToUserCallbackHandler(
                        assignCaseToRespondentSolHelper,
                        objectMapper,
                        generalAppFeesService,
                        coreCaseUserService,
                        caseDetailsConverter,
                        gaForLipService,
                        rolesAndAccessAssignmentService);
    }

    public static final int RESPONDENT_ONE = 0;
    public static final int RESPONDENT_TWO = 1;
    public static final String SPEC_CLAIM = "SPEC_CLAIM";
    public static final String UNSPEC_CLAIM = "UNSPEC_CLAIM";
    public static final String STRING_NUM_CONSTANT = "this is a string";

    @Nested
    class AssignRolesUnspecCase {
        @BeforeEach
        void setup() {
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            GASolicitorDetailsGAspec respondent2 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setDefendant1PartyName("Respondent1")
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setGeneralAppSuperClaimType(UNSPEC_CLAIM)
                    .setIsMultiParty(YES)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;

            Map<String, Object> dataMap =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldCallAssignCase_3Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(3)).assignCase(any(), any(), any(), any());
        }

        @Test
        void shouldThrowExceptionIfSolicitorsAreNull() {
            Exception exception =
                    assertThrows(
                            Exception.class,
                            () ->
                                    assignCaseToUserHandler.handle(
                                            getCaseDateWithNoSolicitor(SPEC_CLAIM)));
            String expectedMessage = "java.lang.NullPointerException";
            String actualMessage = exception.toString();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class AssignRolesUnspecCaseWithOutNoticeApplication {
        @BeforeEach
        void setup() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
            List<Element<GASolicitorDetailsGAspec>> addlApplicantSol = new ArrayList<>();
            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            GASolicitorDetailsGAspec respondent2 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");
            GASolicitorDetailsGAspec addlApplicant1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org1");

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            addlApplicantSol.add(element(addlApplicant1));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setGeneralAppInformOtherParty(
                            new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setGeneralAppApplicantAddlSolicitors(addlApplicantSol)
                    .setDefendant1PartyName("Respondent1")
                    .setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setIsMultiParty(YesOrNo.NO)
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
                    .setGeneralAppSuperClaimType(UNSPEC_CLAIM)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;

            Map<String, Object> dataMap =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});
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
            verify(coreCaseUserService, times(2)).assignCase(any(), any(), any(), any());
        }
    }

    @Nested
    class AssignDefendantRoleForGALip {
        @BeforeEach
        void setup() {
            when(gaForLipService.isLipResp(any())).thenReturn(true);
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec().setId("id").setEmail("test@gmail.com");

            respondentSols.add(element(respondent1));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setIsGaRespondentOneLip(YES)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setIsMultiParty(YesOrNo.NO)
                    .setDefendant1PartyName("Respondent1")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YES))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;
            Map<String, Object> data =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});

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
                    .assignCase(
                            CASE_ID.toString(),
                            generalApplication
                                    .getGeneralAppRespondentSolicitors()
                                    .getFirst()
                                    .getValue()
                                    .getId(),
                            null,
                            CaseRole.DEFENDANT);
        }
    }

    @Nested
    class AssignRolesSpecCase {
        @BeforeEach
        void setup() {
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            GASolicitorDetailsGAspec respondent2 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setIsMultiParty(YES)
                    .setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YES))
                    .setDefendant1PartyName("Respondent1")
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;

            Map<String, Object> dataMap =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldCallAssignCase_3Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(3)).assignCase(any(), any(), any(), any());
        }
    }

    @Nested
    class AssignRolesSpecCaseWithoutNotice {
        @BeforeEach
        void setup() {
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            GASolicitorDetailsGAspec respondent2 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppInformOtherParty(
                            new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setIsMultiParty(YES)
                    .setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
                    .setDefendant1PartyName("Respondent1")
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;

            Map<String, Object> dataMap =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});
            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldNotCallAssignCase() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(0)).assignCase(any(), any(), any(), any());
        }
    }

    @Nested
    class AssignRolesSpecCaseLipResp {
        @BeforeEach
        void setup() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            GASolicitorDetailsGAspec respondent2 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(ADJOURN_HEARING)))
                    .setClaimant1PartyName("Applicant1")
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppInformOtherParty(
                            new GAInformOtherParty().setIsWithNotice(YesOrNo.YES))
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setIsMultiParty(NO)
                    .setIsGaRespondentOneLip(YES)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YesOrNo.YES))
                    .setDefendant1PartyName("Respondent1")
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;

            Map<String, Object> dataMap =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});
            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldNotCallAssignCase() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(0)).assignCase(any(), any(), any(), any());
        }

        @Test
        void shouldHaveDefendantRole() {
            var response =
                    (AboutToStartOrSubmitCallbackResponse) assignCaseToUserHandler.handle(params);
            assertThat(response.getData().get("respondent1OrganisationPolicy"))
                    .extracting("OrgPolicyCaseAssignedRole")
                    .isEqualTo("[DEFENDANT]");
        }
    }

    @Nested
    class AssignRolesSpecCaseLipApp {
        @BeforeEach
        void setup() {
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            respondentSols.add(element(respondent1));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(ADJOURN_HEARING)))
                    .setClaimant1PartyName("Applicant1")
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppInformOtherParty(
                            new GAInformOtherParty().setIsWithNotice(YesOrNo.YES))
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec().setId("id").setEmail("TEST@gmail.com"))
                    .setIsMultiParty(NO)
                    .setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(YES)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YesOrNo.YES))
                    .setDefendant1PartyName("Respondent1")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;

            Map<String, Object> dataMap =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});
            params = callbackParamsOfPendingState(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldReturnSubmittedCallbackResponse() {
            var response = assignCaseToUserHandler.handle(params);
            assertThat(response).isInstanceOf(SubmittedCallbackResponse.class);
        }

        @Test
        void shouldCallAssignCaseLip() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(1)).assignCase(any(), any(), any(), eq(CLAIMANT));
        }
    }

    @Nested
    class AssignRolesSpecCaseWithNotice {
        @BeforeEach
        void setup() {
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            GASolicitorDetailsGAspec respondent2 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setIsMultiParty(YES)
                    .setDefendant1PartyName("Respondent1")
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YES))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;

            Map<String, Object> dataMap =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldCallAssignCase_3Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(3)).assignCase(any(), any(), any(), any());
        }
    }

    @Nested
    class AssignRolesSpecCaseForWithoutNoticeApplication {
        @BeforeEach
        void setup() {
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id3")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            GASolicitorDetailsGAspec respondent2 =
                    new GASolicitorDetailsGAspec()
                            .setId("id2")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");
            GASolicitorDetailsGAspec respondent3 =
                    new GASolicitorDetailsGAspec()
                            .setId("id1")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");
            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            respondentSols.add(element(respondent3));

            GASolicitorDetailsGAspec addlApplicant =
                    new GASolicitorDetailsGAspec()
                            .setId("id1")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("Org1");
            List<Element<GASolicitorDetailsGAspec>> addlApplSols = new ArrayList<>();
            addlApplSols.add(element(addlApplicant));

            GeneralApplication builder = new GeneralApplication();
            builder.setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppApplicantAddlSolicitors(addlApplSols)
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppInformOtherParty(
                            new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setDefendant1PartyName("Respondent1")
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setIsMultiParty(YesOrNo.NO)
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            generalApplication = builder;

            Map<String, Object> dataMap =
                    objectMapper.convertValue(generalApplication, new TypeReference<>() {});
            params = callbackParamsOfPendingState(dataMap, CallbackType.SUBMITTED);
        }

        public List<CaseAssignmentUserRole> getCaseAssignedApplicantUserRoles() {

            return List.of(
                    CaseAssignmentUserRole.builder()
                            .caseDataId("1")
                            .userId(STRING_NUM_CONSTANT)
                            .caseRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build(),
                    CaseAssignmentUserRole.builder()
                            .caseDataId("1")
                            .userId("2")
                            .caseRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build(),
                    CaseAssignmentUserRole.builder()
                            .caseDataId("1")
                            .userId("3")
                            .caseRole(RESPONDENTSOLICITORONE.getFormattedName())
                            .build(),
                    CaseAssignmentUserRole.builder()
                            .caseDataId("1")
                            .userId("4")
                            .caseRole(RESPONDENTSOLICITORONE.getFormattedName())
                            .build(),
                    CaseAssignmentUserRole.builder()
                            .caseDataId("1")
                            .userId("5")
                            .caseRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build());
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOneAndTwoUnspec() {
            assignCaseToUserHandler.handle(params);
            verifyApplicantSolicitorOneSpecRoles();
        }

        @Test
        void shouldCallAssignCase_1Times() {
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(2)).assignCase(any(), any(), any(), any());
        }
    }

    @Nested
    class AssignRoles1V3 {

        Map<String, Object> dataMap;
        Map<String, Object> dataMapWithNotice;

        @BeforeEach
        void setup() {
            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org3");

            GASolicitorDetailsGAspec respondent2 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");
            GASolicitorDetailsGAspec respondent3 =
                    new GASolicitorDetailsGAspec()
                            .setId("id")
                            .setEmail("test@gmail.com")
                            .setOrganisationIdentifier("org2");

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            respondentSols.add(element(respondent3));

            GeneralApplication builder = new GeneralApplication();
            builder.setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO)
                    .setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setIsMultiParty(YES)
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YES))
                    .setDefendant1PartyName("Respondent1")
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY));

            GeneralApplication builderWithNotice = new GeneralApplication();
            builderWithNotice
                    .setGeneralAppType(
                            new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                    .setClaimant1PartyName("Applicant1")
                    .setGeneralAppRespondentSolicitors(respondentSols)
                    .setGeneralAppApplnSolicitor(
                            new GASolicitorDetailsGAspec()
                                    .setId("id")
                                    .setEmail("TEST@gmail.com")
                                    .setOrganisationIdentifier("Org1"))
                    .setIsMultiParty(YES)
                    .setGeneralAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YES))
                    .setDefendant1PartyName("Respondent1")
                    .setClaimant2PartyName("Applicant2")
                    .setDefendant2PartyName("Respondent2")
                    .setGeneralAppSuperClaimType(SPEC_CLAIM)
                    .setGeneralAppParentCaseLink(
                            new GeneralAppParentCaseLink().setCaseReference("12342341"))
                    .setCivilServiceUserRoles(
                            new IdamUserDetails()
                                    .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                    .setEmail("applicant@someorg.com"))
                    .setBusinessProcess(
                            new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                    .setIsGaRespondentOneLip(NO)
                    .setIsGaApplicantLip(NO)
                    .setIsGaRespondentTwoLip(NO);

            generalApplicationWithNotice = builderWithNotice;
            generalApplication = builder;
            dataMapWithNotice =
                    objectMapper.convertValue(
                            generalApplicationWithNotice, new TypeReference<>() {});
            dataMap = objectMapper.convertValue(generalApplication, new TypeReference<>() {});
        }

        @Test
        void shouldCallAssignCaseResp_4TimeAwaitingPayment() {
            params = callbackParamsOf(dataMapWithNotice, CallbackType.ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) assignCaseToUserHandler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCallAssignCaseApp_1TimePending() {
            params = callbackParamsOfPendingState(dataMap, CallbackType.ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) assignCaseToUserHandler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCallAssignCaseResp_4TimesNoPending() throws Exception {
            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

            // print out the raw CaseDetails data so we can inspect the keys
            System.out.println(
                    objectMapper.writeValueAsString(
                            params.getRequest().getCaseDetails().getData()));

            // resp1 sol1 has been assigned twice, one at line 36 then in for loop
            AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) assignCaseToUserHandler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCallAssignCaseRespSubmitted_4TimesNoPending() {
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
            // resp1 sol1 has been assigned twice, one at line 36 then in for loop
            assignCaseToUserHandler.handle(params);
            verify(coreCaseUserService, times(2))
                    .assignCase(any(), any(), any(), eq(RESPONDENTSOLICITORONE));
            verify(coreCaseUserService, times(2))
                    .assignCase(any(), any(), any(), eq(RESPONDENTSOLICITORTWO));
        }
    }

    private void verifyApplicantSolicitorOneRoles() {
        verify(coreCaseUserService)
                .assignCase(
                        CASE_ID.toString(),
                        generalApplication.getGeneralAppApplnSolicitor().getId(),
                        "Org1",
                        CaseRole.APPLICANTSOLICITORONE);
    }

    private void verifyApplicantSolicitorOneSpecRoles() {
        verify(coreCaseUserService)
                .assignCase(
                        CASE_ID.toString(),
                        generalApplication.getGeneralAppApplnSolicitor().getId(),
                        "Org1",
                        CaseRole.APPLICANTSOLICITORONE);
    }

    public CallbackParams getCaseDateWithNoSolicitor(String claimType) {

        GeneralApplication builder = new GeneralApplication();
        builder.setGeneralAppType(
                        new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                .setClaimant1PartyName("Applicant1")
                .setDefendant1PartyName("Respondent1")
                .setClaimant2PartyName("Applicant2")
                .setDefendant2PartyName("Respondent2")
                .setGeneralAppParentCaseLink(
                        new GeneralAppParentCaseLink().setCaseReference("12342341"))
                .setGeneralAppSuperClaimType(claimType)
                .setCivilServiceUserRoles(
                        new IdamUserDetails()
                                .setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                .setEmail("applicant@someorg.com"))
                .setBusinessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY));

        GeneralApplication caseData = builder;

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {});
        return callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
    }
}
