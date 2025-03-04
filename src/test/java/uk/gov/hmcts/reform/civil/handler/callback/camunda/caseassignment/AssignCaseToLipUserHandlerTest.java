package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_CASE_TO_APPLICANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment.AssignCaseToLipUserHandler.TASK_ID;

@ExtendWith(MockitoExtension.class)
public class AssignCaseToLipUserHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private AssignCaseToLipUserHandler assignCaseToLipUserHandler;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private ObjectMapper objectMapper;

    private CallbackParams params;
    private CaseData caseData;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Nested
    class AssignRolesIn1v1LipCase {

        @Test
        void shouldReturnCorrectCamundaTaskID() {
            assertThat(assignCaseToLipUserHandler.camundaActivityId(CallbackParamsBuilder.builder()
                                                                     .request(CallbackRequest.builder().eventId(
                                                                         "ASSIGN_CASE_TO_APPLICANT1").build()).build())).isEqualTo(TASK_ID);
        }

        @ParameterizedTest
        @EnumSource(value = CaseEvent.class,
            names = { "ASSIGN_CASE_TO_APPLICANT1" }, mode = EnumSource.Mode.EXCLUDE
        )
        void shouldThrowExceptionWhenCaseEventIsInvalid(CaseEvent caseEvent) {
            // Given: an invalid event id
            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder().eventId(caseEvent.name()).build()).build();
            // When: I call the camundaActivityId
            // Then: an exception is thrown
            CallbackException ex = assertThrows(CallbackException.class, () -> assignCaseToLipUserHandler.camundaActivityId(callbackParams),
                                                "A CallbackException was expected to be thrown but wasn't.");
            assertThat(ex.getMessage()).contains("Callback handler received illegal event");
        }

        @Test
        void shouldAssignCaseToApplicantAndRemoveCreator() {
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .claimantUserDetails(IdamUserDetails.builder()
                                         .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                         .email("test@gmail.com")
                                         .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });

            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT1.name(), CallbackType.ABOUT_TO_SUBMIT);

            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);

            assignCaseToLipUserHandler.handle(params);

            verifyApplicantOneRoles();
        }
    }

    private void verifyApplicantOneRoles() {
        verify(coreCaseUserService).assignCase(
            caseData.getCcdCaseReference().toString(),
            caseData.getClaimantUserDetails().getId(),
            null,
            CaseRole.CLAIMANT
        );

        verify(coreCaseUserService).removeCreatorRoleCaseAssignment(
            caseData.getCcdCaseReference().toString(),
            caseData.getClaimantUserDetails().getId(),
            null
        );

    }
}
