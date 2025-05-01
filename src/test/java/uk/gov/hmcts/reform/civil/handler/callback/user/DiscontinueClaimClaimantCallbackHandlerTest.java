package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PermissionGranted;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;

@SpringBootTest(classes = {
    DiscontinueClaimClaimantCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class DiscontinueClaimClaimantCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DiscontinueClaimClaimantCallbackHandler handler;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private ObjectMapper objectMapper;
    public static final String PERMISSION_GRANTED_BY_COURT = "# Your request is being reviewed";
    public static final String CASE_DISCONTINUED_FULL_DISCONTINUE = "# Your claim has been discontinued";
    public static final String NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE = "# Your claim will be fully discontinued against the specified defendants";
    public static final String NO_COURT_PERMISSION_PART_DISCONTINUE = "#  We have noted your claim has been partly discontinued and your claim has been updated";
    public static final String PERMISSION_GRANTED_BY_COURT_BODY = "### Next steps \n "
            + "You will be notified of the outcome.\n\n"
            + "You may be contacted by the court to provide more information if necessary.";
    public static final String CASE_DISCONTINUED_FULL_DISCONTINUE_BODY = "### Next step \n "
            + "Any hearing listed will be vacated and all other parties will be notified.";
    public static final String NO_COURT_PERMISSION_PART_DISCONTINUE_BODY = "### Next step \n "
            + "Any listed hearings will still proceed as normal.\n\n"
            + "All other parties will be notified.";
    public static final String NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE_BODY = "### Next step \n "
            + "This will now be reviewed and the claim will proceed offline and your online account will not "
            + "be updated for this claim.Any updates will be sent by post.";

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNullClaimantList_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("claimantWhoIsDiscontinuing")).isNull();
        }

        @Test
        void should_return_error_if_error_list_present() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atState1v2DifferentSolicitorClaimDetailsRespondent1NotifiedTimeExtension().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response.getErrors()).isNotNull();
        }

        @Test
        void shouldPopulateClaimantList_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("claimantWhoIsDiscontinuing")).isNotNull();
        }

        @Test
        void shouldPopulateDefendantListFor1v2LrVLr_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("Resp1").build())
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).partyName("Resp2").build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("discontinuingAgainstOneDefendant")).isNotNull();
        }

        @Test
        void shouldNotPopulateDefendantListFor2v1LrVLr_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("discontinuingAgainstOneDefendant")).isNull();
        }
    }

    @Nested
    class MidEventCheckIfConsentRequiredCallback {

        private static final String PAGE_ID = "showClaimantConsent";

        @Test
        void shouldSetSelectedClaimant_when2v1() {
            DynamicList claimantWhoIsDiscontinuingList = DynamicList.builder()
                .value(DynamicListElement.builder()
                           .label("Both")
                           .build())
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            caseData.setClaimantWhoIsDiscontinuing(claimantWhoIsDiscontinuingList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("selectedClaimantForDiscontinuance")).isNotNull();
        }

        @Test
        void shouldNotPopulateSelectedClaimant_whenClaimNot2v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("selectedClaimantForDiscontinuance")).isNull();
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(DISCONTINUE_CLAIM_CLAIMANT);
    }

    @Nested
    class MidEventCheckPermissionGrantedCallback {

        private static final String PAGE_ID = "checkPermissionGranted";

        @Test
        void shouldHaveNoErrors_when2v1AndPermissionGrantedDataValid() {
            DynamicList claimantWhoIsDiscontinuingList = DynamicList.builder()
                .value(DynamicListElement.builder()
                           .label("Both")
                           .build())
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            caseData.setClaimantWhoIsDiscontinuing(claimantWhoIsDiscontinuingList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldHaveErrors_when2v1AndPermissionDateInFuture() {
            DynamicList claimantWhoIsDiscontinuingList = DynamicList.builder()
                .value(DynamicListElement.builder()
                           .label("Both")
                           .build())
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            caseData.setClaimantWhoIsDiscontinuing(claimantWhoIsDiscontinuingList);
            caseData.setIsPermissionGranted(SettleDiscontinueYesOrNoList.YES);
            caseData.setPermissionGrantedComplex(PermissionGranted.builder()
                                                     .permissionGrantedJudge("Test")
                                                     .permissionGrantedDate(LocalDate.now().plusDays(1))
                                                     .build());

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().size()).isEqualTo(1);
            assertThat(response.getErrors()).containsOnly("Date must be in the past");
        }

        @Test
        void shouldHaveErrors_when2v1AndPermissionNotGranted() {
            DynamicList claimantWhoIsDiscontinuingList = DynamicList.builder()
                .value(DynamicListElement.builder()
                           .label("Both")
                           .build())
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            caseData.setClaimantWhoIsDiscontinuing(claimantWhoIsDiscontinuingList);
            caseData.setIsPermissionGranted(SettleDiscontinueYesOrNoList.NO);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().size()).isEqualTo(1);
            assertThat(response.getErrors()).containsOnly("Unable to discontinue this claim");
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            CaseData caseDataBefore = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(INDIVIDUAL).build())
                .applicant2(Party.builder().type(INDIVIDUAL).build())
                .respondent1(Party.builder().type(INDIVIDUAL).build())
                .respondent2(Party.builder().type(INDIVIDUAL).build())
                .buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);
        }

        @Test
        void shouldUpdateCaseState_WhenNoCourtPermissionAndFullDiscontinue_2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted2v1RespondentUnrepresented().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE)
                    .claimantWhoIsDiscontinuing(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                    .label("Both")
                                    .build()).build())
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.CASE_DISCONTINUED.name());
        }

        @Test
        void shouldUpdateCaseState_WhenNoCourtPermissionAndFullDiscontinue_1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE)
                    .isDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.YES)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.CASE_DISCONTINUED.name());
        }

        @Test
        void shouldUpdateCaseState_WhenNoCourtPermissionAndFullDiscontinue_1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE)
                    .isDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.YES)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.CASE_DISCONTINUED.name());
        }

        @Test
        void shouldNotUpdateCaseState_WhenNotFullDiscontinue() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(caseData.getCcdState().name());
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldShowPermissionGrantedByCourtHeader_WhenCourtPermissionGranted() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                    .isPermissionGranted(SettleDiscontinueYesOrNoList.YES)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.YES).build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(PERMISSION_GRANTED_BY_COURT));
            Assertions.assertTrue(response.getConfirmationBody().contains(PERMISSION_GRANTED_BY_COURT_BODY));
        }

        @Test
        void shouldShowCaseDiscontinuedFDHeader_WhenNoCourtPermissionAndClaimantsBothSelected2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted2v1RespondentUnrepresented().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE)
                    .claimantWhoIsDiscontinuing(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                    .label("Both")
                                    .build()).build())
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(CASE_DISCONTINUED_FULL_DISCONTINUE));
            Assertions.assertTrue(response.getConfirmationBody().contains(CASE_DISCONTINUED_FULL_DISCONTINUE_BODY));
        }

        @Test
        void shouldShowCaseDiscontinuedFDHeader_WhenNoCourtPermissionAndAgainstBothDefendants1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE)
                    .isDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.YES)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(CASE_DISCONTINUED_FULL_DISCONTINUE));
            Assertions.assertTrue(response.getConfirmationBody().contains(CASE_DISCONTINUED_FULL_DISCONTINUE_BODY));
        }

        @Test
        void shouldShowCaseDiscontinuedFDHeader_WhenNoCourtPermission1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE)
                    .isDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.YES)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(CASE_DISCONTINUED_FULL_DISCONTINUE));
            Assertions.assertTrue(response.getConfirmationBody().contains(CASE_DISCONTINUED_FULL_DISCONTINUE_BODY));
        }

        @Test
        void shouldShowNoCourtPermissionFDAnyoneHeader_WhenNoCourtPermissionAndNotAgainstBothDefendants1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE)
                    .isDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.NO)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE));
            Assertions.assertTrue(response.getConfirmationBody().contains(NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE_BODY));
        }

        @Test
        void shouldShowNoCourtPermissionFDAnyoneHeader_WhenNoCourtPermissionAndNotBothClaimantsSelected2v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentUnrepresented().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE)
                    .claimantWhoIsDiscontinuing(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                    .label("claimant 1")
                                    .build()).build())
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE));
            Assertions.assertTrue(response.getConfirmationBody().contains(NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE_BODY));
        }

        @Test
        void shouldShowNoCourtPermissionPDHeader_WhenNoCourtPermissionAndPartDiscountinue2v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentUnrepresented().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                    .claimantWhoIsDiscontinuing(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                    .label("claimant 1")
                                    .build()).build())
                    .isDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.NO)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(NO_COURT_PERMISSION_PART_DISCONTINUE));
            Assertions.assertTrue(response.getConfirmationBody().contains(NO_COURT_PERMISSION_PART_DISCONTINUE_BODY));
        }

        @Test
        void shouldShowNoCourtPermissionPDHeader_When1v2NoCourtPermissionAndPartDiscountinue() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                    .isDiscontinuingAgainstBothDefendants(SettleDiscontinueYesOrNoList.YES)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(NO_COURT_PERMISSION_PART_DISCONTINUE));
            Assertions.assertTrue(response.getConfirmationBody().contains(NO_COURT_PERMISSION_PART_DISCONTINUE_BODY));
        }

        @Test
        void shouldShowNoCourtPermissionPDHeader_WhenNoCourtPermissionAnd1v1PartDiscountinue() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued().build().toBuilder()
                    .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                    (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains(NO_COURT_PERMISSION_PART_DISCONTINUE));
            Assertions.assertTrue(response.getConfirmationBody().contains(NO_COURT_PERMISSION_PART_DISCONTINUE_BODY));
        }
    }
}
