package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.CreateSDOCallbackHandlerUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.DisposalHearingPopulator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.FastTrackPopulator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.PrePopulateOrderDetailsPages;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.PrePopulateSdoR2AndNihlFields;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SmallClaimsPopulator;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;

@ExtendWith(MockitoExtension.class)
public class PrePopulateOrderDetailsPagesTest {

    @Mock
    private LocationReferenceDataService locationRefDataService;
    @Mock
    private WorkingDayIndicator workingDayIndicator;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private LocationHelper locationHelper;
    @Mock
    private SmallClaimsPopulator smallClaimsPopulator;
    @Mock
    private CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils;
    @Mock
    private FastTrackPopulator fastTrackPopulator;
    @Mock
    private DisposalHearingPopulator disposalHearingPopulator;
    @Mock
    private PrePopulateSdoR2AndNihlFields prePopulateSdoR2AndNihlFields;

    private PrePopulateOrderDetailsPages prePopulateOrderDetailsPages;

    private static final BigDecimal TEST_CCMCC_AMOUNT = new BigDecimal("1000");
    private static final String TEST_CCMCC_EPIMMS_ID = "some-epimms-id";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        prePopulateOrderDetailsPages = new PrePopulateOrderDetailsPages(
            objectMapper,
            locationRefDataService,
            workingDayIndicator,
            featureToggleService,
            locationHelper,
            smallClaimsPopulator,
            createSDOCallbackHandlerUtils,
            fastTrackPopulator,
            disposalHearingPopulator,
            prePopulateSdoR2AndNihlFields,
            TEST_CCMCC_AMOUNT,
            TEST_CCMCC_EPIMMS_ID
        );

        when(createSDOCallbackHandlerUtils.getDynamicHearingMethodList(any(CallbackParams.class), any(CaseData.class)))
            .thenReturn(createSampleDynamicList());
    }

    private DynamicList createSampleDynamicList() {
        DynamicListElement inPerson = DynamicListElement.builder()
            .code("IN_PERSON")
            .label("In Person")
            .build();

        DynamicListElement telephone = DynamicListElement.builder()
            .code("TELEPHONE")
            .label("Telephone")
            .build();

        DynamicListElement video = DynamicListElement.builder()
            .code("VIDEO")
            .label("Video")
            .build();

        List<DynamicListElement> listItems = List.of(inPerson, telephone, video);

        return DynamicList.builder()
            .listItems(listItems)
            .value(inPerson)
            .build();
    }

    @Test
    void shouldPrePopulateOrderDetailsPages_whenSdoR2Enabled() {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .version(V_1)
            .params(Map.of(BEARER_TOKEN, "test-token"))
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(List.of(LocationRefData.builder().build()));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) prePopulateOrderDetailsPages.execute(callbackParams);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData().get("smallClaimsMethod")).isEqualTo("smallClaimsMethodInPerson");
        assertThat(response.getData().get("fastTrackMethod")).isEqualTo("fastTrackMethodInPerson");
    }

    @Test
    void shouldPrePopulateOrderDetailsPages_whenSdoR2EnabledWithoutVersion() {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "test-token"))
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(List.of(LocationRefData.builder().build()));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) prePopulateOrderDetailsPages.execute(callbackParams);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData().get("smallClaimsMethod")).isEqualTo("smallClaimsMethodInPerson");
        assertThat(response.getData().get("fastTrackMethod")).isEqualTo("fastTrackMethodInPerson");
    }

    @Test
    void shouldNotPrePopulateOrderDetailsPages_whenSdoR2Disabled() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .version(V_1)
            .params(Map.of(BEARER_TOKEN, "test-token"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) prePopulateOrderDetailsPages.execute(callbackParams);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldPrePopulateOrderDetailsPages_whenCarmEnabledForCase() {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .version(V_1)
            .params(Map.of(BEARER_TOKEN, "test-token"))
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(List.of(LocationRefData.builder().build()));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) prePopulateOrderDetailsPages.execute(callbackParams);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    void shouldUpdateCaseManagementLocationIfSpecClaim1000OrLessAndCcmcc() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(TEST_CCMCC_EPIMMS_ID).region(
                "ccmcRegion").build())
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .version(V_1)
            .params(Map.of(BEARER_TOKEN, "test-token"))
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(List.of(LocationRefData.builder().build()));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) prePopulateOrderDetailsPages.execute(callbackParams);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData().get("smallClaimsMethod")).isEqualTo("smallClaimsMethodInPerson");
        assertThat(response.getData().get("fastTrackMethod")).isEqualTo("fastTrackMethodInPerson");
    }
}
