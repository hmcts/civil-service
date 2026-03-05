package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue.NoticeOfDiscontinuanceFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_NOTICE_OF_DISCONTINUANCE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDiscontinueClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
})
class GenerateDiscontinueClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateDiscontinueClaimCallbackHandler handler;
    @MockBean
    private NoticeOfDiscontinuanceFormGenerator formGenerator;
    @MockBean
    private RuntimeService runTimeService;
    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private OrganisationService organisationService;
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @Nested
    class AboutToSubmitCallback {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldUpdateCamundaVariables_whenInvoked(Boolean toggleState) {
            //Given
            Organisation organisation = new Organisation();
            organisation.setOrganisationID("Id");
            OrganisationPolicy organisationPolicy = new OrganisationPolicy();
            organisationPolicy.setOrganisation(organisation);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .applicant1OrganisationPolicy(organisationPolicy)
                .businessProcess(businessProcess).build();
            caseData.setCourtPermissionNeeded(
                toggleState ? SettleDiscontinueYesOrNoList.YES : SettleDiscontinueYesOrNoList.NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());
            //When
            handler.handle(params);
            //Then
            verify(runTimeService).setVariable(PROCESS_INSTANCE_ID, "JUDGE_ORDER_VERIFICATION_REQUIRED", toggleState);
        }

        @Test
        void shouldGenerateNoticeOfDiscontinueDocForCW_whenCourtPermissionRequired() {
            when(formGenerator.generateDocs(any(CaseData.class), anyString(), any(Address.class), anyString(), anyString(), anyBoolean())).thenReturn(getCaseDocument());
            when(organisationService.findOrganisationById(anyString())).thenReturn(getOrganisation());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1(getRespondent1PartyDetails());
            caseData.setApplicant1(getApplicant1PartyDetails());
            caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.YES);
            caseData.setRespondent1DQ(new Respondent1DQ());
            caseData.setIsPermissionGranted(SettleDiscontinueYesOrNoList.YES);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
            caseData.setBusinessProcess(businessProcess);
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (getOrganisation().isPresent()) {
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             getOrganisation().get().getName(),
                                                             Address.fromContactInformation(getOrganisation()
                                                                                                .get()
                                                                                                .getContactInformation()
                                                                                                .get(0)),
                                                             "claimant",
                                                             "BEARER_TOKEN",
                                                             false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             getOrganisation().get().getName(),
                                                             Address.fromContactInformation(getOrganisation()
                                                                                                .get()
                                                                                                .getContactInformation()
                                                                                                .get(0)),
                                                             "defendant",
                                                             "BEARER_TOKEN",
                                                             false);
            }

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getApplicant1NoticeOfDiscontinueCWViewDoc()).isNotNull();
            assertThat(updatedData.getRespondent1NoticeOfDiscontinueCWViewDoc()).isNotNull();
        }

        @Test
        void shouldGenerateNoticeOfDiscontinueDocForAllParties_whenNoCourtPermissionRequired_1vs2() {
            when(formGenerator.generateDocs(any(CaseData.class), anyString(), any(Address.class), anyString(), anyString(), anyBoolean())).thenReturn(getCaseDocument());
            when(organisationService.findOrganisationById(anyString())).thenReturn(getOrganisationWithoutName());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1(getRespondent1PartyDetails());
            StatementOfTruth statementOfTruth = new StatementOfTruth();
            statementOfTruth.setName("Signer 2 Name");
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQStatementOfTruth(statementOfTruth);
            caseData.setRespondent1DQ(respondent1DQ);
            StatementOfTruth statementOfTruth1 = new StatementOfTruth();
            statementOfTruth1.setName("Signer 3 Name");
            Respondent2DQ respondent2DQ = new Respondent2DQ();
            respondent2DQ.setRespondent2DQStatementOfTruth(statementOfTruth1);
            caseData.setRespondent2DQ(respondent2DQ);
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.NO);
            caseData.setApplicant1(getApplicant1PartyDetails());
            caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.NO);
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
            caseData.setBusinessProcess(businessProcess);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (getOrganisation().isPresent()) {
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Signer Name",
                                                             Address.fromContactInformation(getOrganisation()
                                                                                                .get()
                                                                                                .getContactInformation()
                                                                                                .get(0)),
                                                             "claimant",
                                                             "BEARER_TOKEN",
                                                             false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Signer 2 Name",
                                                             Address.fromContactInformation(getOrganisation()
                                                                                                .get()
                                                                                                .getContactInformation()
                                                                                                .get(0)),
                                                             "defendant1",
                                                             "BEARER_TOKEN",
                                                             false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Signer 3 Name",
                                                             Address.fromContactInformation(getOrganisation()
                                                                                                .get()
                                                                                                .getContactInformation()
                                                                                                .get(0)),
                                                             "defendant2",
                                                             "BEARER_TOKEN",
                                                             false);
            }

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getApplicant1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
            assertThat(updatedData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
        }

        @Test
        void shouldGenerateNoticeOfDiscontinueDocForAllParties_whenNoCourtPermissionRequired_noNames_1vs2() {
            when(formGenerator.generateDocs(any(CaseData.class), anyString(), any(Address.class), anyString(), anyString(), anyBoolean())).thenReturn(getCaseDocument());
            when(organisationService.findOrganisationById(anyString())).thenReturn(getOrganisationWithoutName());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1(getRespondent1PartyDetails());
            caseData.setApplicant1(getApplicant1PartyDetails());
            caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.NO);
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
            caseData.setBusinessProcess(businessProcess);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (getOrganisation().isPresent()) {
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Signer Name",
                                                             Address.fromContactInformation(getOrganisation()
                                                                                                .get()
                                                                                                .getContactInformation()
                                                                                                .get(0)),
                                                             "claimant",
                                                             "BEARER_TOKEN",
                                                             false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "",
                                                             Address.fromContactInformation(getOrganisation()
                                                                                                .get()
                                                                                                .getContactInformation()
                                                                                                .get(0)),
                                                             "defendant",
                                                             "BEARER_TOKEN",
                                                             false);
            }

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getApplicant1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
            assertThat(updatedData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
        }

        @Test
        void shouldGenerateNoticeOfDiscontinueDocForAllParties_whenNoCourtPermissionRequired_LrVsLiP_1vs2() {
            when(formGenerator.generateDocs(any(CaseData.class), anyString(), any(Address.class), anyString(), anyString(), anyBoolean())).thenReturn(getCaseDocument());
            when(organisationService.findOrganisationById(anyString())).thenReturn(getOrganisationWithoutName());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1(getRespondent1PartyDetails());
            caseData.setRespondent2(getRespondent2PartyDetails());
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent2Represented(YesOrNo.NO);
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setApplicant1(getApplicant1PartyDetails());
            caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.NO);
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
            caseData.setBusinessProcess(businessProcess);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (getOrganisation().isPresent()) {
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Signer Name",
                                                             Address.fromContactInformation(getOrganisation()
                                                                                                .get()
                                                                                                .getContactInformation()
                                                                                                .get(0)),
                                                             "claimant",
                                                             "BEARER_TOKEN", false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             getRespondent1PartyDetails().getPartyName(),
                                                             getRespondent1PartyDetails().getPrimaryAddress(),
                                                             "defendant1",
                                                             "BEARER_TOKEN", true);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             getRespondent2PartyDetails().getPartyName(),
                                                             getRespondent2PartyDetails().getPrimaryAddress(),
                                                             "defendant2",
                                                             "BEARER_TOKEN", true);
            }

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getApplicant1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
            assertThat(updatedData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
        }

        @Test
        void shouldGenerateNoticeOfDiscontinueDocForAllParties_whenNoCourtPermissionRequired_1vs2_serviceAddress() {
            when(formGenerator.generateDocs(any(CaseData.class), anyString(), any(Address.class), anyString(), anyString(), anyBoolean())).thenReturn(getCaseDocument());
            when(organisationService.findOrganisationById(anyString())).thenReturn(getOrganisationWithoutName());
            Address serviceAddress = new Address();
            serviceAddress.setAddressLine1("Service");
            serviceAddress.setPostCode("S3RV 1C3");

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1(getRespondent1PartyDetails());
            caseData.setRespondent2(getRespondent2PartyDetails());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.NO);
            caseData.setRespondentSolicitor1ServiceAddress(serviceAddress);
            caseData.setApplicantSolicitor1ServiceAddress(serviceAddress);
            caseData.setRespondentSolicitor2ServiceAddress(serviceAddress);
            caseData.setApplicant1(getApplicant1PartyDetails());
            caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.NO);
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
            caseData.setBusinessProcess(businessProcess);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (getOrganisation().isPresent()) {
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Signer Name",
                                                             serviceAddress,
                                                             "claimant",
                                                             "BEARER_TOKEN", false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "",
                                                             serviceAddress,
                                                             "defendant1",
                                                             "BEARER_TOKEN", false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "",
                                                             serviceAddress,
                                                             "defendant2",
                                                             "BEARER_TOKEN", false);
            }

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getApplicant1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
            assertThat(updatedData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
        }

        @Test
        void shouldGenerateNoticeOfDiscontinueDocForAllParties_whenNoCourtPermissionRequired_1vs2_correspondenceAddress() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            when(formGenerator.generateDocs(any(CaseData.class), anyString(), any(Address.class), anyString(), anyString(), anyBoolean())).thenReturn(getCaseDocument());
            when(organisationService.findOrganisationById(anyString())).thenReturn(getOrganisation());
            Address serviceAddress = new Address();
            serviceAddress.setAddressLine1("Service");
            serviceAddress.setPostCode("S3RV 1C3");
            Address correspondenceAddress = new Address();
            correspondenceAddress.setAddressLine1("Correspondence");
            correspondenceAddress.setPostCode("C0RR 5P0N");

            CaseData caseData = CaseDataBuilder.builder().atStateSpec1v2ClaimSubmitted().build();
            caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
            caseData.setRespondent1(getRespondent1PartyDetails());
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.NO);
            caseData.setSpecApplicantCorrespondenceAddressdetails(correspondenceAddress);
            caseData.setSpecRespondentCorrespondenceAddressdetails(correspondenceAddress);
            caseData.setSpecRespondent2CorrespondenceAddressdetails(correspondenceAddress);
            caseData.setRespondent2(getRespondent2PartyDetails());
            caseData.setRespondentSolicitor1ServiceAddress(serviceAddress);
            caseData.setApplicantSolicitor1ServiceAddress(serviceAddress);
            caseData.setApplicant1(getApplicant1PartyDetails());
            caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.NO);
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
            caseData.setBusinessProcess(businessProcess);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (getOrganisation().isPresent()) {
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Organisation name",
                                                             correspondenceAddress,
                                                             "claimant",
                                                             "BEARER_TOKEN", false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Organisation name",
                                                             correspondenceAddress,
                                                             "defendant1",
                                                             "BEARER_TOKEN", false);
                verify(formGenerator, times(1)).generateDocs(caseData,
                                                             "Organisation name",
                                                             correspondenceAddress,
                                                             "defendant2",
                                                             "BEARER_TOKEN", false);
            }

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getApplicant1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
            assertThat(updatedData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc()).isNotNull();
        }
    }

    @Test
    void shouldSetTheValuesInPreTranslationCollectionForWelshTranslation() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        when(formGenerator.generateDocs(
            any(CaseData.class),
            anyString(), any(Address.class), anyString(), anyString(), anyBoolean()
        )).thenReturn(getCaseDocument());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setRespondent1(getRespondent1PartyDetails());
        caseData.setApplicant1(getApplicant1PartyDetails());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE);
        caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.NO);
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        List<Element<CaseDocument>> translatedDocuments = updatedData.getPreTranslationDocuments();
        assertEquals(1, translatedDocuments.size());
        assertEquals(PreTranslationDocumentType.NOTICE_OF_DISCONTINUANCE, updatedData.getPreTranslationDocumentType());
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.camundaActivityId(params)).isEqualTo("GenerateNoticeOfDiscontinueClaim");
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(GEN_NOTICE_OF_DISCONTINUANCE);
    }

    private Party getRespondent1PartyDetails() {
        return PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("John")
                .individualLastName("Doe")
                .build();
    }

    private Party getRespondent2PartyDetails() {
        return PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("Jane")
                .individualLastName("Doe")
                .build();
    }

    private Party getApplicant1PartyDetails() {
        return PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("Carl")
                .individualLastName("Foster")
                .build();
    }

    private CaseDocument getCaseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setCreatedBy("John");
        caseDocument.setDocumentName("document name");
        caseDocument.setDocumentSize(0L);
        caseDocument.setDocumentType(DIRECTIONS_QUESTIONNAIRE);
        caseDocument.setCreatedDatetime(LocalDateTime.now());
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");
        caseDocument.setDocumentLink(documentLink);
        return  caseDocument;
    }

    private Optional<uk.gov.hmcts.reform.civil.prd.model.Organisation> getOrganisation() {
        return Optional.of(new uk.gov.hmcts.reform.civil.prd.model.Organisation()
                               .setName("Organisation name")
                               .setContactInformation(List.of(new ContactInformation()
                                                               .setAddressLine1("Address 1")
                                                               .setPostCode("Post Code")))
                               );
    }

    private Optional<uk.gov.hmcts.reform.civil.prd.model.Organisation> getOrganisationWithoutName() {
        return Optional.of(new uk.gov.hmcts.reform.civil.prd.model.Organisation()
                               .setContactInformation(List.of(new ContactInformation()
                                                               .setAddressLine1("Address 1")
                                                               .setPostCode("Post Code")))
                               );
    }
}
