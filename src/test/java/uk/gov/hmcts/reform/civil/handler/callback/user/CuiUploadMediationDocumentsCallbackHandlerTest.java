package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;
import uk.gov.hmcts.reform.civil.model.mediation.UploadMediationDocumentsForm;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.REFERRED_DOCUMENTS;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    CuiUploadMediationDocumentsCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class CuiUploadMediationDocumentsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private CuiUploadMediationDocumentsCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private DashboardApiClient dashboardApiClient;

    @MockBean
    private DashboardNotificationsParamsMapper mapper;


    private static final List<MediationDocumentsType> MEDIATION_NON_ATTENDANCE_OPTION = List.of(NON_ATTENDANCE_STATEMENT);
    private static final List<MediationDocumentsType> DOCUMENTS_REFERRED_OPTION = List.of(REFERRED_DOCUMENTS);

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        private static final UploadMediationDocumentsForm EXPECTED_FORM =
            UploadMediationDocumentsForm.builder().build();

        @Nested
        class MediationNonAttendanceDocumentOption {

            @Test
            void shouldUploadRespondent1Documents_whenInvokedForMediationNonAttendance() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationByDocumentTypes(MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<Element<MediationNonAttendanceStatement>> actual = updatedData.getRes1MediationNonAttendanceDocs();

                assertThat(updatedData.getRes1MediationNonAttendanceDocs()).isEqualTo(actual);
                assertThat(updatedData.getRes1MediationNonAttendanceDocs()).hasSize(1);
                verify(dashboardApiClient, times(0)).recordScenario(any(), any(), any(), any());
                verify(mapper, times(0)).mapCaseDataToParams(any());

            }
            @Test
            void shouldUploadRespondent1Documents_whenInvokedForMediationNonAttendance_WithCarmIsEnabled() {
                when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationByDocumentTypes(MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<Element<MediationNonAttendanceStatement>> actual = updatedData.getRes1MediationNonAttendanceDocs();

                assertThat(updatedData.getRes1MediationNonAttendanceDocs()).isEqualTo(actual);
                assertThat(updatedData.getRes1MediationNonAttendanceDocs()).hasSize(1);
                verify(dashboardApiClient, times(2)).recordScenario(any(), any(), any(), any());
                verify(mapper, times(2)).mapCaseDataToParams(any());

            }
        }

        @Nested
        class DocumentsReferredOption {
            @Test
            void shouldUploadRespondent1Documents_whenInvokedForDocumentsReferred() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationByDocumentTypes(DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<Element<MediationDocumentsReferredInStatement>>  actual = updatedData.getRes1MediationDocumentsReferred();

                assertThat(updatedData.getRes1MediationDocumentsReferred()).isEqualTo(actual);
                assertThat(updatedData.getRes1MediationDocumentsReferred()).hasSize(1);
                verify(dashboardApiClient, times(0)).recordScenario(any(), any(), any(), any());
                verify(mapper, times(0)).mapCaseDataToParams(any());

            }

            @Test
            void shouldUploadRespondent1Documents_whenInvokedForDocumentsReferred_withCarmEnabled() {
                when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationByDocumentTypes(DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<Element<MediationDocumentsReferredInStatement>>  actual = updatedData.getRes1MediationDocumentsReferred();

                assertThat(updatedData.getRes1MediationDocumentsReferred()).isEqualTo(actual);
                assertThat(updatedData.getRes1MediationDocumentsReferred()).hasSize(1);
                verify(dashboardApiClient, times(2)).recordScenario(any(), any(), any(), any());
                verify(mapper, times(2)).mapCaseDataToParams(any());

            }

        }

    }

}
