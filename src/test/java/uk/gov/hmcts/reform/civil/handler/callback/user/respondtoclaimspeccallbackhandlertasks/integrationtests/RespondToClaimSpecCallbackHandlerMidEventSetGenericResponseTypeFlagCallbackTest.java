package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RespondToClaimSpecCallbackHandler.class,
        ExitSurveyConfiguration.class,
        ExitSurveyContentService.class,
        JacksonAutoConfiguration.class,
        ValidationAutoConfiguration.class,
        DateOfBirthValidator.class,
        UnavailableDateValidator.class,
        CaseDetailsConverter.class,
        LocationReferenceDataService.class,
        CourtLocationUtils.class,
        SimpleStateFlowEngine.class,
        SimpleStateFlowBuilder.class,
        AssignCategoryId.class,
        FrcDocumentsUtils.class,
        RespondToClaimSpecCallbackHandlerTestConfig.class
})
public class RespondToClaimSpecCallbackHandlerMidEventSetGenericResponseTypeFlagCallbackTest extends BaseCallbackHandlerTest {

    private static final String PAGE_ID = "set-generic-response-type-flag";

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;

    @MockBean
    private PaymentDateValidator validator;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private SimpleStateFlowBuilder simpleStateFlowBuilder;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @MockBean
    private Time time;

    private AboutToStartOrSubmitCallbackResponse handleCallback(CaseData caseData) {
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
    }

    @Test
    void shouldSetMultiPartyResponseTypeFlags_Counter_Admit_OR_Admit_Part_combination1() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondent2v1BothNotFullDefence_PartAdmissionX2()
                .build();

        AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);

        assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
    }

    @Test
    void shouldSetMultiPartyResponseTypeFlags_Counter_Admit_OR_Admit_Part_combination2() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondent2v1BothNotFullDefence_CounterClaimX2()
                .build();

        AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);

        assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
    }

    @Test
    void shouldSetMultiPartyResponseTypeFlags_1v2_sameSolicitor_DifferentResponse() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

        AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);

        assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("FULL_DEFENCE");
        assertThat(response.getData()).extracting("sameSolicitorSameResponse")
                .isEqualTo("Yes");
    }

    @Test
    void shouldSetMultiPartyResponseTypeFlags_AdmitAll_OR_Admit_Part_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondent1v2AdmitAll_AdmitPart()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

        AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);

        assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
    }

    @Test
    void shouldSetMultiPartyResponseTypeFlags_FullDefence_OR_AdmitAll_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondent1v2FullDefence_AdmitPart()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

        AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);

        assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
    }
}
