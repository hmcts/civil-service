package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDate.EPOCH;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_GENERAL_APPLICATION_CASE;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class CreateApplicationTaskHandlerTest {

    private static final String STRING_CONSTANT = "this is a string";
    private static final LocalDate APP_DATE_EPOCH = EPOCH;
    private static final String PROCESS_INSTANCE_ID = "1";
    private static final String CASE_ID = "1";
    private static final String GA_ID = "2";
    private static final String GA_CASE_TYPES = "Summary judgment";
    private static final LocalDateTime DUMMY_DATE = LocalDateTime.parse("2022-02-22T15:59:59");
    private static final UUID DOC_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    List<Element<GeneralApplicationsDetails>> generalApplicationsDetailsList = Lists.newArrayList();
    List<Element<GeneralApplicationsDetails>>  gaDetailsMasterCollection = Lists.newArrayList();
    List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolList = Lists.newArrayList();
    List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolTwoList = Lists.newArrayList();
    List<Element<GeneralApplication>> generalApplications = Lists.newArrayList();
    CaseDataContent caseDataContent = CaseDataContent.builder().build();

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private GaStateFlowEngine gaStateFlowEngine;

    @InjectMocks
    private CreateApplicationTaskHandler createApplicationTaskHandler;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");

        Map<String, Object> variables = Map.of(
            "caseId", CASE_ID,
            "caseEvent", CREATE_GENERAL_APPLICATION_CASE.name()
        );

        when(mockTask.getAllVariables()).thenReturn(variables);

        GaStateFlow stateFlow = mock(GaStateFlow.class);
        State state = mock(State.class);
        when(state.getName()).thenReturn("MAIN.DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.getFlags()).thenReturn(Map.of());
        when(gaStateFlowEngine.evaluate(any(GeneralApplicationCaseData.class))).thenReturn(stateFlow);
    }

    @Nested
    class CreateGAFor2V1and1V2SameSol {
        @Test
        void shouldAddWithOutNoticeGaApplnToExistingClaimantCollection() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, NO, NO, NO);
            GeneralApplicationCaseData data = buildDataWithExistingCollection(generalApplication, YES, NO);
            assertThat(data.getClaimantGaAppDetails()).hasSize(2);
            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).hasSize(2);
        }

        @Test
        void shouldAddWithOutNoticeGaApplnToExistingClaimantCollectionAndIsRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, NO, YES, YES);
            GeneralApplicationCaseData data = buildDataWithExistingCollection(generalApplication, YES, NO);

            assertThat(data.getClaimantGaAppDetails()).hasSize(2);
            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).hasSize(2);
        }

        @Test
        void shouldAddWithNoticeGaApplnToExistingClaimantCollectionAndIsRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, YES, YES, YES);
            GeneralApplicationCaseData data = buildDataWithExistingCollection(generalApplication, YES, NO);

            assertThat(data.getClaimantGaAppDetails()).hasSize(2);
            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).hasSize(2);
        }

        @Test
        void shouldAddWithOutNoticeGaApplnToExistingRespondentOneSolCollection() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, NO, NO, NO);
            GeneralApplicationCaseData data = buildDataWithExistingCollection(generalApplication, YES, NO);

            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolGaAppDetails()).hasSize(2);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).hasSize(2);
        }

        @Test
        void shouldAddWithOutNoticeGaApplnToExistingRespondentOneSolCollectionAndIsRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, NO, YES, YES);
            GeneralApplicationCaseData data = buildDataWithExistingCollection(generalApplication, YES, NO);

            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolGaAppDetails()).hasSize(2);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).hasSize(2);
        }

        @Test
        void shouldAddWithNoticeApplnToClaimantAndVisibleToAllCollections1V2() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, YES, NO, YES);
            GeneralApplicationCaseData data = buildDataWithExistingCollection(generalApplication, NO, NO);

            assertThat(data.getClaimantGaAppDetails()).hasSize(2);
            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).hasSize(2);
        }

        @Test
        void shouldAddWithNoticeApplnToRespondent2SolCollectionAndIsRespAgreed1V2() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent2", NO, YES, YES, YES);
            GeneralApplicationCaseData data = buildDataWithExistingCollection(generalApplication, NO, NO);

            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).hasSize(1);
            assertThat(data.getGaDetailsMasterCollection()).hasSize(2);
        }

        @Test
        void shouldAddWithoutNoticeApplnToRespondentOneCollection1V2SameSol() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, NO, NO, NO);
            GeneralApplicationCaseData data = buildDataWithExistingCollection(generalApplication, NO, YES);

            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolGaAppDetails()).hasSize(2);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).hasSize(2);
        }

        private GeneralApplication getGeneralApplication(String organisationIdentifier,
                                                         YesOrNo parentClaimantIsApplicant,
                                                         YesOrNo isWithoutNotice,
                                                         YesOrNo isRespAgreed,
                                                         YesOrNo isDocumentVisible) {
            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();

            builder.generalAppType(GAApplicationType.builder()
                                       .types(singletonList(SUMMARY_JUDGEMENT))
                                       .build());

            return builder
                .parentClaimantIsApplicant(parentClaimantIsApplicant)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                                   .hasAgreed(isRespAgreed).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .organisationIdentifier(organisationIdentifier).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                                                .isWithNotice(isWithoutNotice)
                                                .reasonsForWithoutNotice(STRING_CONSTANT)
                                                .build())
                .generalAppDateDeadline(DUMMY_DATE)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                                  .generalAppUrgency(YES)
                                                  .reasonsForUrgency(STRING_CONSTANT)
                                                  .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                                  .build())
                .isMultiParty(YES)
                .isDocumentVisibleGA(isDocumentVisible)
                .businessProcess(new BusinessProcess()
                                     .setStatus(STARTED)
                                     .setProcessInstanceId(PROCESS_INSTANCE_ID)
                                     .setCamundaEvent(CREATE_GENERAL_APPLICATION_CASE.name()))
                .build();
        }
    }

    @Nested
    class CreateGeneralApplication {
        /*
         * GA without notice application
         * */

        @Test
        void shouldAddApplicantSolListForWithoutNoticeAppln() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, NO, NO, NO, NO, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).allMatch(element -> element.getValue().getParentClaimantIsApplicant() != null);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();
        }

        @Test
        void shouldAddRespondentOneSolListForWithoutNoticeAppln() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, NO, NO, NO, NO, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddRespondentSolListForWithoutNoticeApplnMultiParty() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, NO, YES, NO, NO, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddRespondentTwoSolListForWithoutNoticeApplnMultiParty() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent2", NO, NO, YES, NO, NO, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        /*
         * GA with notice application
         * */

        @Test
        void shouldAddApplicantSolListForWithNoticeApplnFor1v1Scenario() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, YES, NO, NO, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddApplicantSolListForWithNoticeApplnMultiParty() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, YES, YES, NO, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();
        }

        @Test
        void shouldAddRespondentOneSolListForWithoutNoticeAppln1v1Scenario() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, NO, NO, NO, NO, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolGaAppDetails()).allMatch(element -> element.getValue().getParentClaimantIsApplicant() != null);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();
        }

        @Test
        void shouldAddRespondentOneSolListForWithoutNoticeApplnMultiParty() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, NO, YES, NO, NO, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();
        }

        @Test
        void shouldAddRespondentOneSolListForWithNoticeAppln1v1Scenario() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, YES, NO, NO, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();
        }

        @Test
        void shouldAddRespondentOneSolListForWithNoticeAppln1v1LipScenario() {
            GeneralApplication generalApplication =
                    getGeneralApplication(null, NO, YES, NO, NO, YES, null)
                    .toBuilder().isGaApplicantLip(YES).build();
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();
        }

        @Test
        void shouldAddRespondentTwoSolListForWithNoticeApplnVisibleToAllCollections() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent2", NO, YES, YES, NO, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddRespondentSolListForWithOutNoticeApplnAndGeneralRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, NO, YES, YES, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddRespondentSolListForWithNoticeApplnAndGeneralRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent1", NO, YES, YES, YES, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).hasSize(1);
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddApplicantSolListForWithNoticeApplnAndGeneralRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, YES, YES, YES, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddApplicantSolListForWithoutNoticeApplnAndGeneralRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, NO, YES, YES, YES, null);
            GeneralApplicationCaseData caseData = buildOnlyData(generalApplication, NO, NO).copy()
                .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(null))
                .respondent1OrganisationIDCopy("respondent1").build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
            caseDataContent = CaseDataContent.builder().build();

            when(coreCaseDataService.startUpdate(any(), any()))
                .thenReturn(startEventResponse);

            when(caseDetailsConverter.toGeneralApplicationCaseData(any()))
                .thenReturn(caseData);

            when(coreCaseDataService.caseDataContentFromStartEventResponse(
                any(StartEventResponse.class),
                anyMap()
            )).thenReturn(caseDataContent);

            when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

            Map<String, Object> map = generalApplication.toMap(objectMapper);
            map.put(
                "generalAppNotificationDeadlineDate",
                generalApplication
                    .getGeneralAppDateDeadline()
            );
            map.put(
                "isDocumentVisible",
                generalApplication
                    .getIsDocumentVisibleGA()
            );
            map.put("parentCaseReference", CASE_ID);
            map.put("applicationTypes", GA_CASE_TYPES);

            when(coreCaseDataService.createGeneralAppCase(anyMap())).thenReturn(caseData);

            createApplicationTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).startUpdate(CASE_ID, CREATE_GENERAL_APPLICATION_CASE);

            verify(coreCaseDataService).createGeneralAppCase(map);

            verify(coreCaseDataService).submitUpdate(CASE_ID, caseDataContent);

            GeneralApplicationCaseData data = coreCaseDataService.submitUpdate(CASE_ID, caseDataContent);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddRespondentTwoSolListForWithNoticeApplnAndGeneralRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent2", NO, YES, YES, YES, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        @Test
        void shouldAddRespondentTwoSolListForWithoutNoticeApplnAndGeneralRespAgreed() {
            GeneralApplication generalApplication =
                getGeneralApplication("respondent2", NO, NO, YES, YES, YES, null);
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getRespondentSolTwoGaAppDetails()).hasSize(1);
            assertThat(data.getClaimantGaAppDetails()).isEmpty();
            assertThat(data.getGaDetailsMasterCollection()).isEmpty();

        }

        private GeneralApplication getGeneralApplication(String organisationIdentifier,
                                                         YesOrNo parentClaimantIsApplicant,
                                                         YesOrNo isWithoutNotice, YesOrNo isMultiParty,
                                                         YesOrNo isGeneralAppAgreed,
                                                         YesOrNo isDocumentVisible,
                                                         List<Element<Document>> generalAppEvidenceDocument) {
            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();

            builder.generalAppType(GAApplicationType.builder()
                                       .types(singletonList(SUMMARY_JUDGEMENT))
                                       .build());

            return builder
                .parentClaimantIsApplicant(parentClaimantIsApplicant)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                                   .hasAgreed(isGeneralAppAgreed).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .organisationIdentifier(organisationIdentifier).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                                                .isWithNotice(isWithoutNotice)
                                                .reasonsForWithoutNotice(STRING_CONSTANT)
                                                .build())
                .generalAppDateDeadline(DUMMY_DATE)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                                  .generalAppUrgency(YES)
                                                  .reasonsForUrgency(STRING_CONSTANT)
                                                  .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                                  .build())
                .isMultiParty(isMultiParty)
                .isDocumentVisibleGA(isDocumentVisible)
                .generalAppEvidenceDocument(generalAppEvidenceDocument)
                .businessProcess(new BusinessProcess()
                                     .setStatus(STARTED)
                                     .setProcessInstanceId(PROCESS_INSTANCE_ID)
                                     .setCamundaEvent(CREATE_GENERAL_APPLICATION_CASE.name()))
                .build();
        }

        @Test
        void shouldSetApplicantBilingualFlagClaimantIsApplicant() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, NO, NO, NO, NO, null)
                    .toBuilder()
                    .parentClaimantIsApplicant(YES)
                    .build();
            buildData(generalApplication, NO, NO, false, true, false);
        }

        @Test
        void shouldSetApplicantBilingualFlagDefendantIsApplicant() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, NO, NO, NO, NO, null)
                    .toBuilder()
                    .parentClaimantIsApplicant(NO)
                    .build();
            buildData(generalApplication, NO, NO, false, true, false);
        }

        @Test
        void shouldSetRespondentBilingualFlagClaimantIsApplicant() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, NO, NO, NO, NO, null)
                    .toBuilder()
                    .parentClaimantIsApplicant(YES)
                    .build();
            buildData(generalApplication, NO, NO, false, false, true);
        }

        @Test
        void shouldSetRespondentBilingualFlagDefendantIsApplicant() {
            GeneralApplication generalApplication =
                getGeneralApplication("applicant", YES, NO, NO, NO, NO, null)
                    .toBuilder()
                    .parentClaimantIsApplicant(NO)
                    .build();
            buildData(generalApplication, NO, NO, false, false, true);
        }
    }

    @Nested
    class CreateGeneralApplicationCCDEvent {

        @Test
        void shouldTriggerCCDEvent() {
            GeneralApplication generalApplication = getGeneralApplication();
            GeneralApplicationCaseData data = buildData(generalApplication, NO, NO, false);

            assertThat(data.getCaseNameGaInternal()).isEqualTo("applicant v respondent");
            assertThat(data.getRespondentSolGaAppDetails()).isEmpty();
            assertThat(data.getClaimantGaAppDetails()).hasSize(1);
        }

        @Test
        void shouldNotTriggerCCDEvent() {

            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .businessProcess(new BusinessProcess().setStatus(STARTED)
                                     .setProcessInstanceId(PROCESS_INSTANCE_ID)).build();

            VariableMap variables = Variables.createVariables();
            variables.putValue(uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());
            variables.putValue("generalApplicationCaseId", GA_ID);

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
            CaseDataContent caseDataContent = CaseDataContent.builder().build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails()))
                .thenReturn(caseData);

            when(coreCaseDataService.startUpdate(anyString(), any(CaseEvent.class)))
                .thenReturn(startEventResponse);

            when(coreCaseDataService.caseDataContentFromStartEventResponse(
                any(StartEventResponse.class),
                anyMap()
            )).thenReturn(caseDataContent);

            when(coreCaseDataService.submitUpdate(any(), any()))
                .thenReturn(new GeneralApplicationCaseData().generalAppParentCaseLink(
                    new GeneralAppParentCaseLink().setCaseReference("123")).build());

            createApplicationTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService, times(1)).startUpdate(CASE_ID, CREATE_GENERAL_APPLICATION_CASE);
            verify(coreCaseDataService, never()).createGeneralAppCase(anyMap());
            verify(coreCaseDataService, times(1)).submitUpdate(CASE_ID, caseDataContent);
        }

        @Test
        void shouldTriggerCCDEventWhenAdditionalFieldAdded() {
            GeneralApplication generalApplication = getGeneralApplication();

            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .generalApplications(getGeneralApplications(generalApplication))
                .build();

            VariableMap variables = Variables.createVariables();
            variables.putValue(uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());
            variables.putValue("generalApplicationCaseId", GA_ID);

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
            CaseDataContent caseDataContent = CaseDataContent.builder().build();
            when(coreCaseDataService.startUpdate(CASE_ID, CREATE_GENERAL_APPLICATION_CASE))
                .thenReturn(startEventResponse);

            when(caseDetailsConverter.toGeneralApplicationCaseData(any()))
                .thenReturn(caseData);

            when(coreCaseDataService.caseDataContentFromStartEventResponse(
                any(StartEventResponse.class),
                anyMap()
            )).thenReturn(caseDataContent);

            when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);
            Map<String, Object> map = generalApplication.toMap(objectMapper);
            map.put(
                "generalAppNotificationDeadlineDate",
                generalApplication
                    .getGeneralAppDateDeadline()
            );
            map.put(
                "isDocumentVisible",
                generalApplication
                    .getIsDocumentVisibleGA()
            );
            map.put("parentCaseReference", CASE_ID);
            map.put("applicationTypes", GA_CASE_TYPES);
            when(coreCaseDataService.createGeneralAppCase(anyMap())).thenReturn(caseData);

            when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

            createApplicationTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).createGeneralAppCase(map);

        }

        @Test
        void shouldRemoveGeneralAppFeeToPayInText() {
            GeneralApplication generalApplication = getGeneralApplication();
            GAPbaDetails gaPbaDetails = new GAPbaDetails();
            gaPbaDetails.setGeneralAppPayInformationText("£123.00");
            gaPbaDetails.setFee(new Fee().setCalculatedAmountInPence(new BigDecimal("12300")));
            generalApplication.setGeneralAppPBADetails(gaPbaDetails);

            GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
                .generalApplications(getGeneralApplications(generalApplication))
                .build();

            VariableMap variables = Variables.createVariables();
            variables.putValue(uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());
            variables.putValue("generalApplicationCaseId", GA_ID);

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
            CaseDataContent caseDataContent = CaseDataContent.builder().build();
            when(coreCaseDataService.startUpdate(CASE_ID, CREATE_GENERAL_APPLICATION_CASE))
                .thenReturn(startEventResponse);

            when(caseDetailsConverter.toGeneralApplicationCaseData(any()))
                .thenReturn(caseData);

            when(coreCaseDataService.caseDataContentFromStartEventResponse(
                any(StartEventResponse.class),
                anyMap()
            )).thenReturn(caseDataContent);

            when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

            Map<String, Object> map = generalApplication.toMap(objectMapper);
            map.put(
                "generalAppNotificationDeadlineDate",
                generalApplication
                    .getGeneralAppDateDeadline()
            );
            map.put(
                "isDocumentVisible",
                generalApplication
                    .getIsDocumentVisibleGA()
            );
            map.put("parentCaseReference", CASE_ID);
            map.put("applicationTypes", GA_CASE_TYPES);

            when(coreCaseDataService.createGeneralAppCase(anyMap())).thenReturn(caseData);

            when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

            createApplicationTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).createGeneralAppCase(removeGeneralAppFeeToPayInText(map));

        }

        private Map<String, Object> removeGeneralAppFeeToPayInText(Map<String, Object> map) {
            if (map.get("generalAppPBADetails") instanceof Map<?, ?> generalAppPBADetailsMap) {
                generalAppPBADetailsMap.remove("generalAppFeeToPayInText");
            }
            return map;
        }

        private GeneralApplication getGeneralApplication() {
            GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();

            builder.generalAppType(GAApplicationType.builder()
                                       .types(singletonList(SUMMARY_JUDGEMENT))
                                       .build());

            return builder
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                                                .isWithNotice(NO)
                                                .reasonsForWithoutNotice(STRING_CONSTANT)
                                                .build())
                .generalAppDateDeadline(DUMMY_DATE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                                  .generalAppUrgency(YES)
                                                  .reasonsForUrgency(STRING_CONSTANT)
                                                  .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                                  .build())
                .isMultiParty(NO)
                .isDocumentVisibleGA(NO)
                .parentClaimantIsApplicant(YES)
                .caseNameGaInternal("applicant v respondent")
                .businessProcess(new BusinessProcess()
                                     .setStatus(STARTED)
                                     .setProcessInstanceId(PROCESS_INSTANCE_ID)
                                     .setCamundaEvent(CREATE_GENERAL_APPLICATION_CASE.name()))
                .build();
        }

    }

    public List<Element<GeneralApplication>> getGeneralApplications(GeneralApplication generalApplication) {
        return wrapElements(generalApplication);
    }

    public GeneralApplicationCaseData buildDataWithExistingCollection(GeneralApplication generalApplication, YesOrNo addApplicant2,
                              YesOrNo respondent2SameLegalRepresentative) {
        generalApplications = getGeneralApplications(generalApplication);
        generalApplicationsDetailsList = Lists.newArrayList();
        gaDetailsMasterCollection = Lists.newArrayList();
        gaDetailsRespondentSolList = Lists.newArrayList();
        gaDetailsRespondentSolTwoList = Lists.newArrayList();

        GeneralApplicationsDetails generalApplicationsDetails = GeneralApplicationsDetails.builder()
            .generalApplicationType("Summary judgment")
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState("pending").build();
        generalApplicationsDetailsList.add(element(generalApplicationsDetails));
        gaDetailsMasterCollection.add(element(generalApplicationsDetails));
        GADetailsRespondentSol gaDetailsRespondentSol = GADetailsRespondentSol.builder()
            .generalApplicationType("Summary judgment")
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState("pending").build();
        gaDetailsRespondentSolList.add(element(gaDetailsRespondentSol));
        gaDetailsMasterCollection.add(element(generalApplicationsDetails));

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .respondent1OrganisationPolicy(respondentOrganisationPolicy("respondent1"))
            .respondent2OrganisationPolicy(respondentOrganisationPolicy("respondent2"))
            .ccdState(CaseState.PENDING_APPLICATION_ISSUED)
            .generalApplications(generalApplications)
            .isMultiParty(YES)
            .addApplicant2(addApplicant2)
            .respondent2SameLegalRepresentative(respondent2SameLegalRepresentative)
            .gaDetailsMasterCollection(gaDetailsMasterCollection)
            .generalApplicationsDetails(generalApplicationsDetailsList)
            .gaDetailsRespondentSol(gaDetailsRespondentSolList)
            .gaDetailsRespondentSolTwo(gaDetailsRespondentSolTwoList)
            .businessProcess(new BusinessProcess().setStatus(STARTED)
                                 .setProcessInstanceId(PROCESS_INSTANCE_ID)).build();

        VariableMap variables = Variables.createVariables();
        variables.putValue(uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE, "MAIN.DRAFT");
        variables.putValue(FLOW_FLAGS, Map.of());
        variables.putValue("generalApplicationCaseId", GA_ID);

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
        caseDataContent = CaseDataContent.builder().build();

        when(coreCaseDataService.startUpdate(CASE_ID, CREATE_GENERAL_APPLICATION_CASE))
            .thenReturn(startEventResponse);

        when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails()))
            .thenReturn(caseData);

        when(coreCaseDataService.caseDataContentFromStartEventResponse(
            any(StartEventResponse.class),
            anyMap()
        )).thenReturn(caseDataContent);

        when(coreCaseDataService.submitUpdate(CASE_ID, caseDataContent)).thenReturn(caseData);

        Map<String, Object> map = generalApplication.toMap(objectMapper);
        map.put(
            "generalAppNotificationDeadlineDate",
            generalApplication
                .getGeneralAppDateDeadline()
        );
        map.put(
            "isDocumentVisible", generalApplication.getIsDocumentVisibleGA());
        map.put("parentCaseReference", CASE_ID);
        map.put("applicationTypes", GA_CASE_TYPES);
        when(coreCaseDataService.createGeneralAppCase(anyMap())).thenReturn(caseData);

        createApplicationTaskHandler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, CREATE_GENERAL_APPLICATION_CASE);

        verify(coreCaseDataService).createGeneralAppCase(map);

        verify(coreCaseDataService).submitUpdate(CASE_ID, caseDataContent);

        return coreCaseDataService.submitUpdate(CASE_ID, caseDataContent);
    }

    public GeneralApplicationCaseData buildData(GeneralApplication generalApplication, YesOrNo addApplicant2,
                              YesOrNo respondent2SameLegalRepresentative,
                              boolean addEvidenceDoc) {
        return buildData(generalApplication, addApplicant2, respondent2SameLegalRepresentative,
                         addEvidenceDoc, false, false);
    }

    public GeneralApplicationCaseData buildData(GeneralApplication generalApplication, YesOrNo addApplicant2,
                              YesOrNo respondent2SameLegalRepresentative,
                              boolean addEvidenceDoc, boolean claimantBilingual, boolean defendantBilingual) {
        generalApplications = getGeneralApplications(generalApplication);
        generalApplicationsDetailsList = Lists.newArrayList();
        gaDetailsMasterCollection = Lists.newArrayList();
        gaDetailsRespondentSolList = Lists.newArrayList();
        gaDetailsRespondentSolTwoList = Lists.newArrayList();
        Element<Document> same = Element.<Document>builder()
                .id(DOC_ID)
                .value(new Document().setDocumentUrl("string")).build();
        List<Element<Document>> generalAppEvidenceDocument = addEvidenceDoc ? (new ArrayList<>() {{
                add(same);
            }
        }) : null;

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .respondent1OrganisationPolicy(respondentOrganisationPolicy("respondent1"))
            .respondent2OrganisationPolicy(respondentOrganisationPolicy("respondent2"))
            .ccdState(CaseState.PENDING_APPLICATION_ISSUED)
            .generalApplications(generalApplications)
            .isMultiParty(YES)
            .addApplicant2(addApplicant2)
            .respondent2SameLegalRepresentative(respondent2SameLegalRepresentative)
            .gaDetailsMasterCollection(gaDetailsMasterCollection)
            .generalApplicationsDetails(generalApplicationsDetailsList)
            .gaDetailsRespondentSol(gaDetailsRespondentSolList)
            .gaDetailsRespondentSolTwo(gaDetailsRespondentSolTwoList)
            .businessProcess(new BusinessProcess().setStatus(STARTED)
            .setProcessInstanceId(PROCESS_INSTANCE_ID)).build();
        caseData = caseData.copy()
                .generalAppEvidenceDocument(generalAppEvidenceDocument).build();
        caseData = caseData.copy()
            .claimantBilingualLanguagePreference(claimantBilingual ? "BOTH" : null)
            .respondent1LiPResponse(defendantBilingual
            ? new RespondentLiPResponse().setRespondent1ResponseLanguage("BOTH")
            : null)
            .build();
        VariableMap variables = Variables.createVariables();
        variables.putValue(BaseExternalTaskHandler.FLOW_STATE, "MAIN.DRAFT");
        variables.putValue(FLOW_FLAGS, Map.of());
        variables.putValue("generalApplicationCaseId", GA_ID);

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
        caseDataContent = CaseDataContent.builder().build();

        when(coreCaseDataService.startUpdate(any(), any()))
            .thenReturn(startEventResponse);

        when(caseDetailsConverter.toGeneralApplicationCaseData(any()))
            .thenReturn(caseData);

        when(coreCaseDataService.caseDataContentFromStartEventResponse(
            any(StartEventResponse.class),
            anyMap()
        )).thenReturn(caseDataContent);

        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        Map<String, Object> map = generalApplication.toMap(objectMapper);
        map.put(
            "generalAppNotificationDeadlineDate",
            generalApplication
                .getGeneralAppDateDeadline()
        );

        map.put(
            "isDocumentVisible", generalApplication.getIsDocumentVisibleGA());
        map.put("parentCaseReference", CASE_ID);
        map.put("applicationTypes", GA_CASE_TYPES);
        if (generalApplication.getParentClaimantIsApplicant() == YES) {
            if (claimantBilingual) {
                map.put("applicantBilingualLanguagePreference", YES);
            }
            if (defendantBilingual) {
                map.put("respondentBilingualLanguagePreference", YES);
            }
        } else {
            if (claimantBilingual) {
                map.put("respondentBilingualLanguagePreference", YES);
            }
            if (defendantBilingual) {
                map.put("applicantBilingualLanguagePreference", YES);
            }
        }

        when(coreCaseDataService.createGeneralAppCase(anyMap())).thenReturn(caseData);

        createApplicationTaskHandler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, CREATE_GENERAL_APPLICATION_CASE);
        if (!addEvidenceDoc) {
            verify(coreCaseDataService).createGeneralAppCase(map);
        }

        return coreCaseDataService.submitUpdate(CASE_ID, caseDataContent);
    }

    public GeneralApplicationCaseData buildOnlyData(GeneralApplication generalApplication, YesOrNo addApplicant2,
                              YesOrNo respondent2SameLegalRepresentative) {
        generalApplications = getGeneralApplications(generalApplication);
        generalApplicationsDetailsList = Lists.newArrayList();
        gaDetailsMasterCollection = Lists.newArrayList();
        gaDetailsRespondentSolList = Lists.newArrayList();
        gaDetailsRespondentSolTwoList = Lists.newArrayList();

        return new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .respondent1OrganisationPolicy(respondentOrganisationPolicy("respondent1"))
            .respondent2OrganisationPolicy(respondentOrganisationPolicy("respondent2"))
            .ccdState(CaseState.PENDING_APPLICATION_ISSUED)
            .generalApplications(generalApplications)
            .isMultiParty(YES)
            .addApplicant2(addApplicant2)
            .respondent2SameLegalRepresentative(respondent2SameLegalRepresentative)
            .gaDetailsMasterCollection(gaDetailsMasterCollection)
            .generalApplicationsDetails(generalApplicationsDetailsList)
            .gaDetailsRespondentSol(gaDetailsRespondentSolList)
            .gaDetailsRespondentSolTwo(gaDetailsRespondentSolTwoList)
            .businessProcess(new BusinessProcess().setStatus(STARTED)
                                 .setProcessInstanceId(PROCESS_INSTANCE_ID)).build();
    }

    private OrganisationPolicy respondentOrganisationPolicy(String organisationId) {
        OrganisationPolicy organisationPolicy = new OrganisationPolicy();
        organisationPolicy.setOrganisation(new Organisation().setOrganisationID(organisationId));
        return organisationPolicy;
    }
}
