package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.civil.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@SpringBootTest(classes = {
    CreateSDOCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    MockDatabaseConfiguration.class,
    WorkingDayIndicator.class,
    DeadlinesCalculator.class,
    ValidationAutoConfiguration.class,
    LocationHelper.class,
    AssignCategoryId.class,
    CreateSDOCallbackHandlerTestConfig.class},
    properties = {"reference.database.enabled=false"})
class CreateSDOMidEventDisposalHearingLocationRefDataCallbackTest extends BaseCallbackHandlerTest {

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SdoGeneratorService sdoGeneratorService;

    @MockBean
    private PublicHolidaysCollection publicHolidaysCollection;

    @MockBean
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CreateSDOCallbackHandler handler;

    @Nested
    class MidEventDisposalHearingLocationRefDataCallback extends LocationRefSampleDataBuilder {

        private final LocalDate today = LocalDate.now();

        @BeforeEach
        void commonSetup() {
            given(locationRefDataService.getHearingCourtLocations(any())).willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(today, 5)).thenReturn(today.plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                    .thenReturn(today.plusDays(7));
        }

        @Test
        void shouldPrePopulateDisposalHearingPage() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);
            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                    "Site 1 - Adr 1 - AAA 111",
                    "Site 2 - Adr 2 - BBB 222",
                    "Site 3 - Adr 3 - CCC 333"
            );
        }

        @Test
        void shouldPrePopulateDisposalHearingPageSpec1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                    .toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .totalClaimAmount(BigDecimal.valueOf(10000))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);
            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                    "Site 1 - Adr 1 - AAA 111",
                    "Site 2 - Adr 2 - BBB 222",
                    "Site 3 - Adr 3 - CCC 333"
            );
        }

        @Test
        void shouldPrePopulateDisposalHearingPageSpec2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                    .toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .totalClaimAmount(BigDecimal.valueOf(10000))
                    .applicant1DQ(Applicant1DQ.builder()
                            .applicant1DQRequestedCourt(RequestedCourt.builder().build())
                            .build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);
            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                    "Site 1 - Adr 1 - AAA 111",
                    "Site 2 - Adr 2 - BBB 222",
                    "Site 3 - Adr 3 - CCC 333"
            );
        }

        @Test
        void shouldPrePopulateDisposalHearingPageSpec3() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                    .toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .totalClaimAmount(BigDecimal.valueOf(10000))
                    .applicant1DQ(Applicant1DQ.builder()
                            .applicant1DQRequestedCourt(RequestedCourt.builder()
                                    .responseCourtCode("court3")
                                    .caseLocation(CaseLocationCivil.builder().baseLocation("dummy base").region("dummy region").build())
                                    .build())
                            .build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);
            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                    "Site 1 - Adr 1 - AAA 111",
                    "Site 2 - Adr 2 - BBB 222",
                    "Site 3 - Adr 3 - CCC 333"
            );
            assertThat(dynamicList.getValue().getLabel()).isEqualTo("Site 3 - Adr 3 - CCC 333");
        }
    }
}
