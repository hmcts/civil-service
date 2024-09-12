package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;

@ExtendWith(MockitoExtension.class)
public class SetGenericResponseTypeFlagTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    @InjectMocks
    private SetGenericResponseTypeFlag setGenericResponseTypeFlag;

    @BeforeEach
    void setUp() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
    }

    @Test
    void shouldSetGenericResponseTypeFlagForOneVOneScenario() {
        CaseData caseData = CaseData.builder().build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "Bearer token"))
            .build();

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_ONE);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

            assertNotNull(response);
        }
    }

    @Test
    void shouldSetGenericResponseTypeFlagForTwoVOneScenario() {
        CaseData caseData = CaseData.builder().build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "Bearer token"))
            .build();

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(TWO_V_ONE);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

            assertNotNull(response);
        }
    }

    @Test
    void shouldSetGenericResponseTypeFlagForOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder().build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "Bearer token"))
            .build();

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

            assertNotNull(response);
        }
    }

    @Test
    void shouldSetGenericResponseTypeFlagForOneVTwoTwoLegalRepScenario() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1234L).build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "Bearer token"))
            .build();

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_TWO_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

            assertNotNull(response);
        }
    }
}
