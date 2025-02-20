package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRaiseQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RaiseQueryCallbackHandler.INVALID_CASE_STATE_ERROR;

@ExtendWith(MockitoExtension.class)
class RaiseQueryCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String USER_ID = "UserId";
    private static final Long CASE_ID = Long.parseLong("1234123412341234");
    private static final String QUERY_ID = "QueryId";
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 3, 1, 7, 0, 0);

    @InjectMocks
    private RaiseQueryCallbackHandler handler;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(queryManagementRaiseQuery);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        public void shouldNotReturnError_whenClaimIssued() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_ISSUED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        public void shouldReturnError_whenClaimPendingIssued() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.PENDING_CASE_ISSUED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseDismissedState() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CASE_DISMISSED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseOffline() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }

        @Test
        public void shouldReturnError_whenCaseClosed() {
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .ccdState(CaseState.CLOSED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).containsOnly(INVALID_CASE_STATE_ERROR);
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setupTest() {
            objectMapper.registerModule(new JavaTimeModule());
            when(userService.getUserInfo(any())).thenReturn(UserInfo.builder().uid(USER_ID).build());
            handler = new RaiseQueryCallbackHandler(
                objectMapper, userService, coreCaseUserService
            );
        }

        @Test
        public void shouldPopulateLatestQueryWithApplicantQueryData() {
            when(coreCaseUserService.getUserCaseRoles(CASE_ID.toString(), USER_ID)).thenReturn(List.of(
                APPLICANTSOLICITORONE.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmApplicantSolicitorQueries(mockQueriesCollection(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("queryId")
                .isEqualTo(QUERY_ID);
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("isHearingRelated")
                .isEqualTo(YES.getLabel());
        }

        @Test
        public void shouldPopulateLatestQueryWithRespondent1QueryData() {
            when(coreCaseUserService.getUserCaseRoles(
                CASE_ID.toString(),
                USER_ID
            )).thenReturn(List.of(RESPONDENTSOLICITORONE.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmRespondentSolicitor1Queries(mockQueriesCollection(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("queryId")
                .isEqualTo(QUERY_ID);
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("isHearingRelated")
                .isEqualTo(YES.getLabel());
        }

        @Test
        public void shouldPopulateLatestQueryWithRespondent2QueryData() {
            when(coreCaseUserService.getUserCaseRoles(
                CASE_ID.toString(),
                USER_ID
            )).thenReturn(List.of(RESPONDENTSOLICITORTWO.name()));
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(CASE_ID)
                .qmRespondentSolicitor2Queries(mockQueriesCollection(QUERY_ID, NOW))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("queryId")
                .isEqualTo(QUERY_ID);
            assertThat(response.getData())
                .extracting("qmLatestQuery")
                .extracting("isHearingRelated")
                .isEqualTo(YES.getLabel());
        }

        private CaseQueriesCollection mockQueriesCollection(String queryId, LocalDateTime latestDate) {
            return CaseQueriesCollection.builder()
                .partyName("partyName")
                .roleOnCase("roleOnCase")
                .caseMessages(
                    List.of(
                        Element.<CaseMessage>builder()
                            .id(UUID.randomUUID())
                            .value(
                                CaseMessage.builder()
                                    .id(queryId)
                                    .isHearingRelated(YES)
                                    .createdOn(latestDate)
                                    .build()).build(),
                        Element.<CaseMessage>builder()
                            .id(UUID.randomUUID())
                            .value(
                                CaseMessage.builder()
                                    .id("old-query-id")
                                    .isHearingRelated(NO)
                                    .createdOn(latestDate.minusMinutes(10))
                                    .build()).build()
                    ))
                .build();
        }
    }
}
