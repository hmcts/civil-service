package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Party.Type;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class PopulateRespondent1CopyTest {

    private PopulateRespondent1Copy populateRespondent1Copy;

    @Mock
    private UserService userService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private CourtLocationUtils courtLocationUtils;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private InterestCalculator interestCalculator;

    private ObjectMapper objectMapper;

    private CaseData caseData;
    private UserInfo userInfo;
    private StateFlow baseFlow;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        populateRespondent1Copy = new PopulateRespondent1Copy(
            userService,
            coreCaseUserService,
            toggleService,
            courtLocationUtils,
            objectMapper,
            stateFlowEngine,
            respondToClaimSpecUtils,
            interestCalculator
        );
        caseData = CaseData.builder()
            .respondent1(Party.builder().type(Type.INDIVIDUAL).build())
            .respondent2(Party.builder().type(Type.INDIVIDUAL).build())
            .ccdCaseReference(1234L)
            .build();

        userInfo = UserInfo.builder()
            .uid("testUserId")
            .build();

        baseFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(baseFlow);
    }

    @Test
    void shouldPopulateRespondent1Copy() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);
        when(userService.getUserInfo(any())).thenReturn(userInfo);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

        Map<CallbackParams.Params, Object> params =  new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testBearerToken");

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(params)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) populateRespondent1Copy.execute(
            callbackParams);

        Map<String, Object> responseData = response.getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);
        assertEquals(caseData.getRespondent1(), updatedCaseData.getRespondent1Copy());
        assertEquals(YES, updatedCaseData.getShowCarmFields());
    }

    @Test
    void shouldReturnErrorWhenDefendantResponseAlreadySubmittedForSolicitorOne() {
        when(userService.getUserInfo(any())).thenReturn(userInfo);
        // TWO_RESPONDENT_REPRESENTATIVES flag true and solicitor one has case role
        when(baseFlow.isFlagSet(any())).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

        // respondent1 has already submitted
        caseData = caseData.toBuilder()
            .respondent1ResponseDate(java.time.LocalDateTime.now())
            .build();

        Map<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testBearerToken");

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(params)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) populateRespondent1Copy
            .execute(callbackParams);

        assertTrue(response.getErrors().contains(PopulateRespondent1Copy.ERROR_DEFENDANT_RESPONSE_SPEC_SUBMITTED));
    }

    @Test
    void shouldPopulateInterestAndTotalsInUpdateCarmFields() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);
        when(userService.getUserInfo(any())).thenReturn(userInfo);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

        // interest calculation mocked
        when(interestCalculator.calculateInterest(any())).thenReturn(new java.math.BigDecimal("12.34"));

        caseData = caseData.toBuilder()
            .totalClaimAmount(new java.math.BigDecimal("200"))
            .build();

        Map<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testBearerToken");

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(params)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) populateRespondent1Copy
            .execute(callbackParams);

        CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);
        // claimAmountInPounds with scale(2)
        assertEquals(new java.math.BigDecimal("200.00"), updated.getTotalClaimAmountPlusInterest());
        assertEquals("200.00", updated.getTotalClaimAmountPlusInterestString());
        // totalClaimAmount + interest (both scaled)
        assertEquals(new java.math.BigDecimal("212.34"), updated.getTotalClaimAmountPlusInterestAdmitPart());
        assertEquals("212.34", updated.getTotalClaimAmountPlusInterestAdmitPartString());
    }

    @Test
    void shouldNotShowCarmFieldsWhenToggleIsDisabled() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(false);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);
        when(userService.getUserInfo(any())).thenReturn(userInfo);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

        Map<CallbackParams.Params, Object> params =  new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testBearerToken");

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(params)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) populateRespondent1Copy.execute(
            callbackParams);

        Map<String, Object> responseData = response.getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);
        assertEquals(NO, updatedCaseData.getShowCarmFields());
    }

    @Test
    void shouldHandleOneVTwoTwoLegalRepScenario() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);
        when(userService.getUserInfo(any())).thenReturn(userInfo);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(
            "RESPONDENTSOLICITORONE",
            "RESPONDENTSOLICITORTWO"
        ));

        Map<CallbackParams.Params, Object> params =  new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testBearerToken");

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(params)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) populateRespondent1Copy.execute(
            callbackParams);

        Map<String, Object> responseData = response.getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);
        assertEquals(caseData.getRespondent1(), updatedCaseData.getRespondent1Copy());
        assertEquals(caseData.getRespondent2(), updatedCaseData.getRespondent2Copy());
        assertEquals(YES, updatedCaseData.getShowCarmFields());
    }

    @Test
    void shouldHandleOneVOneScenario() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);

        caseData = CaseData.builder()
            .respondent1(Party.builder().type(Type.INDIVIDUAL).build())
            .respondent2(null)
            .build();

        Map<CallbackParams.Params, Object> params =  new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testBearerToken");

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(params)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) populateRespondent1Copy.execute(callbackParams);

        Map<String, Object> responseData = response.getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);
        assertEquals(caseData.getRespondent1(), updatedCaseData.getRespondent1Copy());
        assertNull(updatedCaseData.getRespondent2Copy());
        assertEquals(YES, updatedCaseData.getShowCarmFields());
    }

    @Test
    void shouldHandleOneVTwoOneLegalRepScenario() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);

        caseData = CaseData.builder()
            .respondent1(Party.builder().type(Type.INDIVIDUAL).build())
            .respondent2(Party.builder().type(Type.INDIVIDUAL).build())
            .respondent2SameLegalRepresentative(YES)
            .ccdCaseReference(1234L)
            .build();

        Map<CallbackParams.Params, Object> params =  new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testBearerToken");

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(params)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) populateRespondent1Copy.execute(callbackParams);

        Map<String, Object> responseData = response.getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);

        assertEquals(caseData.getRespondent1(), updatedCaseData.getRespondent1Copy());
        assertEquals(caseData.getRespondent2(), updatedCaseData.getRespondent2Copy());

        assertEquals(YES, updatedCaseData.getShowCarmFields());
    }

    @Test
    void shouldAddRespondent1TagWhenSolicitorOneRoleIsPresent() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);

        caseData = CaseData.builder()
            .respondent1(Party.builder().type(Type.INDIVIDUAL).build())
            .respondent2(null)
            .ccdCaseReference(1234L)
            .build();

        Map<CallbackParams.Params, Object> params =  new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testBearerToken");

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(params)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) populateRespondent1Copy.execute(callbackParams);

        Map<String, Object> responseData = response.getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);

        Set<DefendantResponseShowTag> showTags = updatedCaseData.getShowConditionFlags();
        assertTrue(showTags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1));

        assertEquals(caseData.getRespondent1(), updatedCaseData.getRespondent1Copy());
        assertNull(updatedCaseData.getRespondent2Copy());
        assertEquals(YES, updatedCaseData.getShowCarmFields());
    }
}
