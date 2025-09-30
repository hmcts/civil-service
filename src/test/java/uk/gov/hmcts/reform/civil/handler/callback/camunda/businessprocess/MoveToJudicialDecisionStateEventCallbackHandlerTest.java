package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PDFBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.applicationdraft.GeneralApplicationDraftGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    MoveToJudicialDecisionStateEventCallbackHandler.class,
    CaseDetailsConverter.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
})
class MoveToJudicialDecisionStateEventCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private ParentCaseUpdateHelper parentCaseUpdateHelper;
    @MockBean
    private Time time;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private GeneralApplicationDraftGenerator generalApplicationDraftGenerator;
    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private GaForLipService gaForLipService;
    @Autowired
    private MoveToJudicialDecisionStateEventCallbackHandler handler;
    private static final String STRING_CONSTANT = "STRING_CONSTANT";
    private static final Long CHILD_CCD_REF = 1646003133062762L;
    private static final Long PARENT_CCD_REF = 1645779506193000L;
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String DUMMY_TELEPHONE_NUM = "234345435435";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRespondWithStateChanged() {
            CaseData caseData = getSampleGeneralApplicationCaseData(YES, NO, YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(gaForLipService.isGaForLip(any())).thenReturn(false);
            when(generalApplicationDraftGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalApplicationDraftGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            assertThat(updatedData.getGaDraftDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
            assertThat(response.getState()).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.toString());
        }

        @Test
        void shouldRespondWithStateChangedWithNoDocumentGeneration() {
            CaseData caseData = getSampleGeneralApplicationCaseData(YES, NO, YES);
            when(gaForLipService.isGaForLip(any())).thenReturn(false);
            CaseData updatedCaseData = caseData.toBuilder().judicialDecision(GAJudicialDecision.builder()
                                                                                 .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                                                                 .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            verifyNoInteractions(generalApplicationDraftGenerator);
            assertThat(response.getState()).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.toString());
        }

        @Test
        void shouldRespondWithStateChangedWithNoDocumentGenerationWhenLipCaseWithJudicial() {
            CaseData caseData = getSampleGeneralApplicationCaseData(YES, NO, YES);
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            CaseData updatedCaseData = caseData.toBuilder().judicialDecision(GAJudicialDecision.builder()
                                                                                 .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                                                                 .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            verifyNoInteractions(generalApplicationDraftGenerator);
            assertThat(response.getState()).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.toString());
        }

        @Test
        void shouldRespondWithStateChangedWithNoDocumentGenerationWhenLipCase() {
            CaseData caseData = getSampleGeneralApplicationCaseData(YES, NO, YES);
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response.getErrors()).isNull();
            verifyNoInteractions(generalApplicationDraftGenerator);
            assertThat(response.getState()).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.toString());
        }
    }

    private CaseData getSampleGeneralApplicationCaseData(YesOrNo isConsented, YesOrNo isTobeNotified, YesOrNo isUrgent) {
        return CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                getGeneralApplication(isConsented, isTobeNotified, isUrgent))
            .toBuilder()
            .claimant1PartyName("Test Claimant1 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .ccdCaseReference(CHILD_CCD_REF)
            .build();
    }

    private GeneralApplication getGeneralApplication(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                     YesOrNo isUrgent) {
        DynamicListElement location1 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("Site Name 2 - Address2 - 28000").build();
        return GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder().types(List.of(RELIEF_FROM_SANCTIONS)).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
            .generalAppPBADetails(
                GAPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(isUrgent).build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferredLocation(DynamicList.builder()
                                                                        .listItems(List.of(location1))
                                                                        .value(location1).build())
                                          .vulnerabilityQuestionsYesOrNo(YES)
                                          .vulnerabilityQuestion("dummy2")
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.MINUTES_30)
                                          .hearingDetailsEmailID(DUMMY_EMAIL)
                                          .hearingDetailsTelephoneNumber(DUMMY_TELEPHONE_NUM).build())
            .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                             .email("abc@gmail.com").build()))
            .isMultiParty(NO)
            .parentClaimantIsApplicant(YES)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(PARENT_CCD_REF.toString()).build())
            .build();
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldDispatchBusinessProcess_whenStatusIsReady() {
            CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1234L).build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            handler.handle(params);

            verify(parentCaseUpdateHelper, times(1)).updateParentWithGAState(
                caseData,
                APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.getDisplayedValue()
            );
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }
}
