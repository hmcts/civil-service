package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAEvidence;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;

import java.time.LocalDate;

import static java.time.LocalDate.EPOCH;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.HOUR_1;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    InitiateGeneralApplicationHandler.class,
    JacksonAutoConfiguration.class,
})
class InitiateGeneralApplicationHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private InitiateGeneralApplicationHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InitiateGeneralApplicationService initiateGeneralAppService;

    private static final String STRING_CONSTANT = "this is a string";
    private static final String STRING_NUM_CONSTANT = "123456789";
    private static final LocalDate APP_DATE_EPOCH = EPOCH;

    private CaseData getTestCaseData(CaseData caseData) {
        return caseData.toBuilder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO)
                                               .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .applicantsPbaAccountsList(STRING_NUM_CONSTANT)
                                      .pbaReference(STRING_CONSTANT)
                                      .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                                            .name(STRING_CONSTANT)
                                            .role(STRING_CONSTANT)
                                            .build())
            .generalAppUploadEvidences(GAEvidence.builder()
                                           .evidenceDocument(wrapElements(Document.builder()
                                                                              .documentUrl(STRING_CONSTANT).build()))
                                           .build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .judgeName(STRING_CONSTANT)
                                          .hearingDate(APP_DATE_EPOCH)
                                          .trialDateFrom(APP_DATE_EPOCH)
                                          .trialDateTo(APP_DATE_EPOCH)
                                          .hearingYesorNo(YES)
                                          .hearingDuration(HOUR_1)
                                          .supportRequirement(singletonList(OTHER_SUPPORT))
                                          .judgeRequiredYesOrNo(YES)
                                          .trialRequiredYesOrNo(YES)
                                          .hearingDetailsEmailID(STRING_CONSTANT)
                                          .unavailableTrailDateTo(APP_DATE_EPOCH)
                                          .supportRequirementOther(STRING_CONSTANT)
                                          .hearingPreferredLocation(DynamicList.builder().build())
                                          .unavailableTrailDateFrom(APP_DATE_EPOCH)
                                          .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                          .reasonForPreferredHearingType(STRING_CONSTANT)
                                          .telephoneHearingPreferredType(STRING_CONSTANT)
                                          .supportRequirementSignLanguage(STRING_CONSTANT)
                                          .hearingPreferencesPreferredType(IN_PERSON)
                                          .unavailableTrailRequiredYesOrNo(YES)
                                          .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                                          .build())
            .build();
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmit {
        @Test
        void shouldAddNewApplicationToList_whenInvoked() {
            when(initiateGeneralAppService.buildCaseData(any(
                uk.gov.hmcts.reform.civil.model.CaseData.CaseDataBuilder.class), any(CaseData.class)))
                .thenCallRealMethod();
            CaseData caseData = getTestCaseData(CaseDataBuilder.builder().build());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertResponse(objectMapper.convertValue(response.getData(), CaseData.class));
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(INITIATE_GENERAL_APPLICATION);
        }

        private void assertResponse(CaseData responseData) {
            assertThat(responseData)
                .extracting("generalApplications")
                .isNotNull();
            GeneralApplication application = unwrapElements(responseData.getGeneralApplications()).get(0);
            assertThat(application.getGeneralAppType().getTypes().contains(EXTEND_TIME)).isTrue();
            assertThat(application.getGeneralAppRespondentAgreement().getHasAgreed()).isEqualTo(NO);
            assertThat(application.getGeneralAppPBADetails().getApplicantsPbaAccountsList())
                .isEqualTo(STRING_NUM_CONSTANT);
            assertThat(application.getGeneralAppDetailsOfOrder()).isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppReasonsOfOrder()).isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppInformOtherParty().getReasonsForWithoutNotice())
                .isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate())
                .isEqualTo(APP_DATE_EPOCH);
            assertThat(application.getGeneralAppStatementOfTruth().getName()).isEqualTo(STRING_CONSTANT);
            assertThat(unwrapElements(application.getEvidenceDocument()).get(0).getDocumentUrl())
                .isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppHearingDetails().getSupportRequirement()
                           .contains(OTHER_SUPPORT)).isTrue();
            assertThat(application.getGeneralAppHearingDetails().getHearingDuration()).isEqualTo(HOUR_1);
            assertThat(application.getGeneralAppHearingDetails().getHearingPreferencesPreferredType())
                .isEqualTo(IN_PERSON);
            assertThat(application.getIsMultiParty()).isEqualTo(NO);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnEmptyResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
        }
    }
}
