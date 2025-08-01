package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PermissionGranted;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_NOTICE_OF_DISCONTINUANCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_DISCONTINUE_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE;

@ExtendWith(MockitoExtension.class)
public class ValidateDiscontinueClaimClaimantCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String UNABLE_TO_VALIDATE = "# Unable to validate information";
    private static final String INFORMATION_SUCCESSFULLY_VALIDATED = "# Information successfully validated";
    private static final String NEXT_STEPS = """
        ### Next steps:

        No further action required.""";
    @Mock
    FeatureToggleService featureToggleService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @InjectMocks
    private ValidateDiscontinueClaimClaimantCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldHandleNullValues_WhenAboutToStartIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getData()).extracting("permissionGrantedJudgeCopy")
                .isNull();
            assertThat(response.getData()).extracting("permissionGrantedDateCopy")
                .isNull();

        }

        @Test
        void shouldPopulateJudgeAndDateCopies_WhenAboutToStartIsInvoked() {
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setPermissionGrantedComplex(PermissionGranted.builder()
                                                     .permissionGrantedJudge("Judge Name")
                                                     .permissionGrantedDate(LocalDate.parse("2022-02-01"))
                                                     .build());
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            //Then
            assertThat(response.getData()).extracting("permissionGrantedJudgeCopy")
                .isEqualTo("Judge Name");
            assertThat(response.getData()).extracting("permissionGrantedDateCopy")
                .isEqualTo("2022-02-01");
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldDoNothing_WhenTypeOfDiscontinuanceIsNullAndAboutToSubmitIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getState()).isNull();
            assertThat(updatedData.getConfirmOrderGivesPermission()).isNull();
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT.name());
        }

        @Test
        void shouldNotChangeCaseState_When1v2FullDiscontAgainstBothDefButNoPermissionAndAboutToSubmitIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(Party.builder().partyName("Resp2").type(Party.Type.INDIVIDUAL).build()).build();
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE);
            caseData.setIsDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.YES);
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getState()).isNull();
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT.name());
        }

        @Test
        void shouldUpdateCaseWithoutStateChange_WhenPartDiscontinuanceAndAboutToSubmitIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE);
            caseData.setIsDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.YES);
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getState()).isNull();
            assertThat(updatedData.getConfirmOrderGivesPermission()).isEqualTo(ConfirmOrderGivesPermission.YES);
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT.name());
        }

        @Test
        void shouldUpdateCaseWithoutStateChange_When1v2FullDiscontinuanceAgainstOneDefAndAboutToSubmitIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(Party.builder().partyName("Resp2").type(Party.Type.INDIVIDUAL).build()).build();
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE);
            caseData.setIsDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.NO);
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getState()).isNull();
            assertThat(updatedData.getConfirmOrderGivesPermission()).isEqualTo(ConfirmOrderGivesPermission.YES);
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT.name());
        }

        @Test
        void shouldDiscontinueCase_When1v2FullDiscontinuanceAgainstBothDefAndAboutToSubmitIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(Party.builder().partyName("Resp2").type(Party.Type.INDIVIDUAL).build()).build();
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE);
            caseData.setIsDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.YES);
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getState()).isEqualTo(CaseState.CASE_DISCONTINUED.name());
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT.name());
        }

        @Test
        void shouldDiscontinueCase_When1v1FullDiscontinuanceAndAboutToSubmitIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE);
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getState()).isEqualTo(CaseState.CASE_DISCONTINUED.name());
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT.name());
        }

        @Test
        void shouldDiscontinueCase_When2v1FullDiscontinuanceAgainstBothClaimantAndAboutToSubmitIsInvoked() {
            //Given
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE);
            caseData.setSelectedClaimantForDiscontinuance("Both");
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getState()).isEqualTo(CaseState.CASE_DISCONTINUED.name());
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT.name());
        }

        @Test
        void shouldSetTheValuesInPreTranslationCollectionForWelshTranslation() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1(PartyBuilder.builder().individual().build().toBuilder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .build())
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .individualFirstName("Doe")
                                .individualLastName("John")
                                .build())
                .respondent1Represented(YesOrNo.NO)
                .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                .respondent1NoticeOfDiscontinueCWViewDoc(CaseDocument.builder()
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
                                                             .build())
                .confirmOrderGivesPermission(ConfirmOrderGivesPermission.YES)
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("BOTH")
                                                             .build()).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_NOTICE_OF_DISCONTINUANCE.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            List<Element<CaseDocument>> translatedDocuments = updatedData.getPreTranslationDocuments();
            assertEquals(1, translatedDocuments.size());
            assertEquals(
                PreTranslationDocumentType.NOTICE_OF_DISCONTINUANCE,
                updatedData.getPreTranslationDocumentType()
            );
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldShowUnableToValidate_WhenNoPermissionAndSubmittedIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.NO);
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            //When
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertTrue(response.getConfirmationHeader().contains(UNABLE_TO_VALIDATE));
            Assertions.assertTrue(response.getConfirmationBody().contains(NEXT_STEPS));
        }

        @Test
        void shouldShowSuccessfullyValidated_WhenPermissionAndSubmittedIsInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.YES);
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            //When
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertTrue(response.getConfirmationHeader().contains(INFORMATION_SUCCESSFULLY_VALIDATED));
            Assertions.assertTrue(response.getConfirmationBody().contains(NEXT_STEPS));
        }
    }
}
