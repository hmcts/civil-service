package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    private ObjectMapper objectMapper;

    private CaseData caseData;
    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        populateRespondent1Copy = new PopulateRespondent1Copy(
            userService,
            coreCaseUserService,
            toggleService,
            courtLocationUtils,
            objectMapper,
            respondToClaimSpecUtils
            );
        caseData = CaseData.builder()
            .respondent1(Party.builder().type(Type.INDIVIDUAL).build())
            .respondent2(Party.builder().type(Type.INDIVIDUAL).build())
            .ccdCaseReference(1234L)
            .build();

        userInfo = UserInfo.builder()
            .uid("testUserId")
            .build();
    }

    @Test
    void shouldPopulateRespondent1Copy() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);
        when(userService.getUserInfo(any())).thenReturn(userInfo);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

        Map<CallbackParams.Params, Object> params = new HashMap<>();
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
    void shouldNotShowCarmFieldsWhenToggleIsDisabled() {
        when(toggleService.isCarmEnabledForCase(any())).thenReturn(false);
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(null);
        when(userService.getUserInfo(any())).thenReturn(userInfo);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

        Map<CallbackParams.Params, Object> params = new HashMap<>();
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

        Map<CallbackParams.Params, Object> params = new HashMap<>();
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

        Map<CallbackParams.Params, Object> params = new HashMap<>();
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

        Map<CallbackParams.Params, Object> params = new HashMap<>();
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

        Map<CallbackParams.Params, Object> params = new HashMap<>();
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
