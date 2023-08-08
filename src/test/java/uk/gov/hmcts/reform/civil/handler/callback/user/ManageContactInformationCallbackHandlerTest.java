package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.utils.PartyDetailsChangedUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    ManageContactInformationCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class ManageContactInformationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private PartyDetailsChangedUtil partyDetailsChangedUtil;
    @Autowired
    private ManageContactInformationCallbackHandler handler;
    @Autowired
    private ObjectMapper objectMapper;

    private static final UserInfo ADMIN_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-admin"))
        .build();
    private static final UserInfo LEGAL_REP_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-solicitor"))
        .build();

    @Nested
    class AboutToStart {

        @Test
        void shouldNotReturnReturnErrors_WhenAboutToStartIsInvokedByAdminUserWhileCaseInAwaitingApplicantIntentionState() {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            CaseData caseData = CaseData.builder().ccdState(CaseState.AWAITING_APPLICANT_INTENTION).build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertNull(response.getErrors());
        }

        @Test
        void shouldReturnErrors_WhenAboutToStartIsInvokedByNonAdminUserWhileCaseInAwaitingApplicantIntentionState() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            CaseData caseData = CaseData.builder().ccdState(CaseState.AWAITING_APPLICANT_INTENTION).build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected =
                List.of("You will be able run the manage contact information event once the claimant has responded.");

            assertEquals(expected, response.getErrors());
        }

        @Test
        void shouldNotReturnErrors_WhenAboutToStartIsInvokedByNonAdminUserWhileCaseInANonAwaitingApplicantIntentionState() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            CaseData caseData = CaseData.builder().ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION).build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertNull(response.getErrors());
        }

        @Nested
        class AboutToSubmit {

            private static final ContactDetailsUpdatedEvent EVENT =
                ContactDetailsUpdatedEvent.builder()
                    .summary("Summary")
                    .description("Description")
                    .build();

            @Test
            void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withPartyChanges() {
                CaseData current = CaseDataBuilder.builder()
                    .applicant1(PartyBuilder.builder().individual().build())
                    .build();
                CaseData updated = current.toBuilder()
                    .applicant1(current.getApplicant1().toBuilder()
                                    .individualFirstName("Dis")
                                    .individualLastName("Guy")
                                    .build())
                    .build();

                when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
                when(partyDetailsChangedUtil.buildChangesEvent(any(CaseData.class), any(CaseData.class))).thenReturn(
                    EVENT);

                CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT, current.toMap(objectMapper));

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertEquals(updated.getApplicant1(), responseData.getApplicant1());
                assertEquals(MANAGE_CONTACT_INFORMATION.name(), responseData.getBusinessProcess().getCamundaEvent());
                assertEquals(READY, responseData.getBusinessProcess().getStatus());
                assertEquals(
                    EVENT.toBuilder().submittedByCaseworker(YES).build(), responseData.getContactDetailsUpdatedEvent());
            }

            @Test
            void shouldReturnExpectedResponseCaseData_whenTriggeredByNonAdmin_withPartyChanges() {
                CaseData current = CaseDataBuilder.builder()
                    .applicant1(PartyBuilder.builder().individual().build())
                    .build();
                CaseData updated = current.toBuilder()
                    .applicant1(current.getApplicant1().toBuilder()
                                    .individualFirstName("Dis")
                                    .individualLastName("Guy")
                                    .build())
                    .build();

                when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
                when(partyDetailsChangedUtil.buildChangesEvent(any(CaseData.class), any(CaseData.class))).thenReturn(
                    EVENT);

                CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT, current.toMap(objectMapper));

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

                assertEquals(updated.getApplicant1(), responseData.getApplicant1());
                assertEquals(MANAGE_CONTACT_INFORMATION.name(), responseData.getBusinessProcess().getCamundaEvent());
                assertEquals(READY, responseData.getBusinessProcess().getStatus());
                assertEquals(
                    EVENT.toBuilder().submittedByCaseworker(NO).build(), responseData.getContactDetailsUpdatedEvent());
            }

            @Test
            void shouldReturnExpectedEmptyCallbackResponse__withNoPartyChanges() {
                CaseData current = CaseDataBuilder.builder()
                    .applicant1(PartyBuilder.builder().individual().build())
                    .build();
                CaseData updated = current.toBuilder().build();

                when(partyDetailsChangedUtil.buildChangesEvent(any(CaseData.class), any(CaseData.class)))
                    .thenReturn(null);

                CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT, current.toMap(objectMapper));

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertEquals(AboutToStartOrSubmitCallbackResponse.builder().build(), response);
            }
        }

        @Nested
        class Submitted {

            @Test
            void shouldReturnExpectedResponse_WhenSubmittedIsInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertEquals(AboutToStartOrSubmitCallbackResponse.builder().build(), response);
            }
        }
    }
}
