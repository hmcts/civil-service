package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class PopulateCaseDataTaskTest {

    @InjectMocks
    PopulateCaseDataTask task;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private CourtLocationUtils courtLocationUtils;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private PaymentDateService paymentDateService;

    @Mock
    private ResponseOneVOneShowTagService responseOneVOneShowTagService;

    @Mock
    private DeadlineExtensionCalculatorService deadlineCalculatorService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        task = new PopulateCaseDataTask(locationRefDataService, objectMapper,
                                        courtLocationUtils, featureToggleService,
                                        paymentDateService, responseOneVOneShowTagService, deadlineCalculatorService);
    }

    @Test
    void shouldUpdateCaseData() {

        CaseData caseData = CaseData.builder()
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondent2DocumentURL("test-respondent2Doc-url")
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        when(featureToggleService.isCarmEnabledForCase(any(CaseData.class))).thenReturn(true);
        when(paymentDateService.getPaymentDateAdmittedClaim(any())).thenReturn(LocalDate.EPOCH);
        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

        CallbackParams params = callbackParams(caseData).toBuilder()
            .version(CallbackVersion.V_2)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(params);

        assertNotNull(response);
        assertEquals(YES, getCaseData(response).getShowCarmFields());
        assertEquals(CaseCategory.SPEC_CLAIM, getCaseData(response).getCaseAccessCategory());
        verify(featureToggleService, times(2)).isPinInPostEnabled();
        verify(featureToggleService, times(1)).isCarmEnabledForCase(any(CaseData.class));

    }

    @Test
    void shouldUpdatedCaseDataPartiallyWhenFeatureTogglesAreFalse() {
        CaseData caseData = CaseData.builder()
            .applicant1DQ(null)
            .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondent2DocumentURL("test-respondent2Doc-url")
            .build();

        when(featureToggleService.isCarmEnabledForCase(any(CaseData.class))).thenReturn(false);
        when(featureToggleService.isPinInPostEnabled()).thenReturn(false);

        CallbackParams params = callbackParams(caseData).toBuilder()
            .version(CallbackVersion.V_1)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(params);

        assertNotNull(response);
        assertEquals(NO, getCaseData(response).getShowCarmFields());
        assertEquals(CaseCategory.SPEC_CLAIM, getCaseData(response).getCaseAccessCategory());
        verify(featureToggleService, times(1)).isPinInPostEnabled();

    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    private CallbackParams callbackParams(CaseData caseData) {

        return CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }
}
