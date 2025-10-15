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
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
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
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE;

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
    @MockitoBean
    private NoticeOfDiscontinuanceFormGenerator formGenerator;
    @MockitoBean
    private RuntimeService runTimeService;
    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private OrganisationService organisationService;
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @Nested
    class AboutToSubmitCallback {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldUpdateCamundaVariables_whenInvoked(Boolean toggleState) {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(Organisation.builder()
                                                                    .organisationID("Id")
                                                                    .build())
                                                  .build())
                .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build()).build();
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

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1(getRespondent1PartyDetails())
                    .applicant1(getApplicant1PartyDetails())
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.YES)
                .respondent1DQ(Respondent1DQ.builder().build())
                    .isPermissionGranted(SettleDiscontinueYesOrNoList.YES)
                    .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
                    .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                    .build();
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

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1(getRespondent1PartyDetails())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQStatementOfTruth(StatementOfTruth.builder()
                                                                      .name("Signer 2 Name")
                                                                      .build())
                                   .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQStatementOfTruth(StatementOfTruth.builder()
                                                                      .name("Signer 3 Name")
                                                                      .build())
                                   .build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .applicant1(getApplicant1PartyDetails())
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO)
                    .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                    .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
                    .build();
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

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1(getRespondent1PartyDetails())
                .applicant1(getApplicant1PartyDetails())
                .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO)
                .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
                .build();
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

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1(getRespondent1PartyDetails())
                .respondent2(getRespondent2PartyDetails())
                .respondent1Represented(YesOrNo.NO)
                .respondent2Represented(YesOrNo.NO)
                .addRespondent2(YesOrNo.YES)
                .applicant1(getApplicant1PartyDetails())
                .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO)
                .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
                .build();
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
            Address serviceAddress = Address.builder().addressLine1("Service").postCode("S3RV 1C3").build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1(getRespondent1PartyDetails())
                .respondent2(getRespondent2PartyDetails())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondentSolicitor1ServiceAddress(serviceAddress)
                .applicantSolicitor1ServiceAddress(serviceAddress)
                .respondentSolicitor2ServiceAddress(serviceAddress)
                .applicant1(getApplicant1PartyDetails())
                .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO)
                .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
                .build();
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
            Address serviceAddress = Address.builder().addressLine1("Service").postCode("S3RV 1C3").build();
            Address correspondenceAddress =
                Address.builder().addressLine1("Correspondence").postCode("C0RR 5P0N").build();

            CaseData caseData = CaseDataBuilder.builder().atStateSpec1v2ClaimSubmitted().build().toBuilder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1(getRespondent1PartyDetails())
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .specApplicantCorrespondenceAddressdetails(correspondenceAddress)
                .specRespondentCorrespondenceAddressdetails(correspondenceAddress)
                .specRespondent2CorrespondenceAddressdetails(correspondenceAddress)
                .respondent2(getRespondent2PartyDetails())
                .respondentSolicitor1ServiceAddress(serviceAddress)
                .applicantSolicitor1ServiceAddress(serviceAddress)
                .applicant1(getApplicant1PartyDetails())
                .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO)
                .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
                .build();
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent1(getRespondent1PartyDetails())
            .applicant1(getApplicant1PartyDetails())
            .respondent1Represented(YesOrNo.NO)
            .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
            .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH")
                                                         .build()).build())
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .build();

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
        return CaseDocument.builder()
                .createdBy("John")
                .documentName("document name")
                .documentSize(0L)
                .documentType(NOTICE_OF_DISCONTINUANCE)
                .createdDatetime(LocalDateTime.now())
                .documentLink(Document.builder()
                        .documentUrl("fake-url")
                        .documentFileName("file-name")
                        .documentBinaryUrl("binary-url")
                        .build())
                .build();
    }

    private Optional<uk.gov.hmcts.reform.civil.prd.model.Organisation> getOrganisation() {
        return Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                               .name("Organisation name")
                               .contactInformation(List.of(ContactInformation.builder()
                                                               .addressLine1("Address 1")
                                                               .postCode("Post Code")
                                                               .build()))
                               .build());
    }

    private Optional<uk.gov.hmcts.reform.civil.prd.model.Organisation> getOrganisationWithoutName() {
        return Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                               .contactInformation(List.of(ContactInformation.builder()
                                                               .addressLine1("Address 1")
                                                               .postCode("Post Code")
                                                               .build()))
                               .build());
    }
}
