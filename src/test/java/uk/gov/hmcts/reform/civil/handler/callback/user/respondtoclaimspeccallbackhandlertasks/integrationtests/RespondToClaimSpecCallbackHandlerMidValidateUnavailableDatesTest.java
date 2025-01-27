package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
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
public class RespondToClaimSpecCallbackHandlerMidValidateUnavailableDatesTest extends BaseCallbackHandlerTest {

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

    @MockBean
    private UnavailableDateValidator dateValidator;

    private CaseData buildCaseDataWithUnavailableDates() {
        List<Element<UnavailableDate>> dates = Stream.of(
                UnavailableDate.builder()
                        .date(LocalDate.of(2024, 5, 2))
                        .who("who 1")
                        .build()).map(ElementUtils::element).toList();
        return CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .respondent1DQ(Respondent1DQ.builder()
                        .respondent1DQHearingSmallClaim(SmallClaimHearing.builder()
                                .unavailableDatesRequired(YES)
                                .smallClaimUnavailableDate(dates)
                                .build())
                        .build())
                .build();
    }

    private CaseData buildCaseDataWithoutUnavailableDates() {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .respondent1DQ(Respondent1DQ.builder()
                        .respondent1DQHearingSmallClaim(SmallClaimHearing.builder()
                                .unavailableDatesRequired(YES)
                                .build())
                        .build())
                .build();
    }

    @Test
    public void testValidateRespondentExpertsMultiparty() {
        CaseData caseData = buildCaseDataWithUnavailableDates();
        Mockito.when(dateValidator.validateSmallClaimsHearing(any())).thenReturn(null);
        CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    public void shouldThrowError_whenValidateRespondentExpertsMultipartyWithNoUnavailableDates() {
        CaseData caseData = buildCaseDataWithoutUnavailableDates();
        List<String> errors = Collections.singletonList("error 1");
        Mockito.when(dateValidator.validateSmallClaimsHearing(any())).thenReturn(errors);
        CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotEmpty();
    }
}
