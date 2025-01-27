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
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
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
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

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
public class RespondToClaimSpecCallbackHandlerMidValidateMediationUnavailabiltyDatesTest extends BaseCallbackHandlerTest {

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

    private CaseData createBaseCaseData() {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(NO)
                .respondent1DQ()
                .build();
    }

    private List<Element<UnavailableDate>> createUnavailableDates(LocalDate singleDate, LocalDate fromDate, LocalDate toDate) {
        return Stream.of(
                UnavailableDate.builder()
                        .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                        .date(singleDate)
                        .build(),
                UnavailableDate.builder()
                        .unavailableDateType(UnavailableDateType.DATE_RANGE)
                        .fromDate(fromDate)
                        .toDate(toDate)
                        .build()
        ).map(ElementUtils::element).toList();
    }

    private CallbackParams createCallbackParams(CaseData caseData, MediationAvailability mediationAvailability, boolean isResp1) {
        CaseData updatedCaseData;
        if (isResp1) {
            updatedCaseData = caseData.toBuilder()
                    .resp1MediationAvailability(mediationAvailability)
                    .build();
        } else {
            updatedCaseData = caseData.toBuilder()
                    .resp2MediationAvailability(mediationAvailability)
                    .build();
        }
        return callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
    }

    @Test
    public void testValidateResp2UnavailableDateWhenAvailabilityIsNo() {
        CaseData caseData = createBaseCaseData();
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(NO)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, false);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void testValidateResp2UnavailableDateWhenAvailabilityIsYesAndSingleDate() {
        CaseData caseData = createBaseCaseData();
        List<Element<UnavailableDate>> unavailableDates = createUnavailableDates(
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6)
        );
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(YES)
                .unavailableDatesForMediation(unavailableDates)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, false);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void testValidateResp1UnavailableDateWhenAvailabilityIsNo() {
        CaseData caseData = createBaseCaseData();
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(NO)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, true);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void testValidateResp1UnavailableDateWhenAvailabilityIsYesAndSingleDate() {
        CaseData caseData = createBaseCaseData();
        List<Element<UnavailableDate>> unavailableDates = createUnavailableDates(
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6)
        );
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(YES)
                .unavailableDatesForMediation(unavailableDates)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, true);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void testValidateResp1UnavailableDateWhenAvailabilityIsYesAndSingleDateErrored() {
        CaseData caseData = createBaseCaseData();
        List<Element<UnavailableDate>> unavailableDates = createUnavailableDates(
                LocalDate.now().minusDays(4),
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6)
        );
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(YES)
                .unavailableDatesForMediation(unavailableDates)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, true);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("Unavailability Date must not be before today.");
    }

    @Test
    public void testResp1UnavailableDateWhenAvailabilityIsYesAndSingleDateIsBeyondYear() {
        CaseData caseData = createBaseCaseData();
        List<Element<UnavailableDate>> unavailableDates = createUnavailableDates(
                LocalDate.now().plusMonths(4),
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6)
        );
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(YES)
                .unavailableDatesForMediation(unavailableDates)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, true);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("Unavailability Date must not be more than three months in the future.");
    }

    @Test
    public void testResp1UnavailableDateWhenDateToIsBeforeDateFrom() {
        CaseData caseData = createBaseCaseData();
        List<Element<UnavailableDate>> unavailableDates = createUnavailableDates(
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6),
                LocalDate.now().plusDays(4)
        );
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(YES)
                .unavailableDatesForMediation(unavailableDates)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, true);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("Unavailability Date From cannot be after Unavailability Date To. Please enter valid range.");
    }

    @Test
    public void testResp1UnavailableDateWhenDateFromIsBeforeToday() {
        CaseData caseData = createBaseCaseData();
        List<Element<UnavailableDate>> unavailableDates = createUnavailableDates(
                LocalDate.now().plusDays(4),
                LocalDate.now().minusDays(6),
                LocalDate.now().plusDays(4)
        );
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(YES)
                .unavailableDatesForMediation(unavailableDates)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, true);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("Unavailability Date From must not be before today.");
    }

    @Test
    public void testResp1UnavailableDateWhenDateToIsBeforeToday() {
        CaseData caseData = createBaseCaseData();
        List<Element<UnavailableDate>> unavailableDates = createUnavailableDates(
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6),
                LocalDate.now().minusDays(4)
        );
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(YES)
                .unavailableDatesForMediation(unavailableDates)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, true);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("Unavailability Date From cannot be after Unavailability Date To. Please enter valid range.");
    }

    @Test
    public void testResp1UnavailableDateWhenDateToIsBeyondOneYear() {
        CaseData caseData = createBaseCaseData();
        List<Element<UnavailableDate>> unavailableDates = createUnavailableDates(
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6),
                LocalDate.now().plusMonths(4)
        );
        MediationAvailability mediationAvailability = MediationAvailability.builder()
                .isMediationUnavailablityExists(YES)
                .unavailableDatesForMediation(unavailableDates)
                .build();
        CallbackParams params = createCallbackParams(caseData, mediationAvailability, true);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("Unavailability Date To must not be more than three months in the future.");
    }
}
