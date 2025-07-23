package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.CreateSDOCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

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
class CreateSDOAboutToSubmitCallbackVariableCaseTest extends BaseCallbackHandlerTest {

    private final LocalDateTime submittedDate = LocalDateTime.now();
    @MockBean
    protected LocationReferenceDataService locationRefDataService;
    @MockBean
    private Time time;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private SdoGeneratorService sdoGeneratorService;
    @MockBean
    private PublicHolidaysCollection publicHolidaysCollection;
    @MockBean
    private NonWorkingDaysCollection nonWorkingDaysCollection;
    @MockBean
    private CategoryService categoryService;
    @Autowired
    private CreateSDOCallbackHandler handler;

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(submittedDate);
    }

    private CaseData buildCaseData() {
        List<String> items = List.of("label 1", "label 2", "label 3");
        DynamicList localOptions = DynamicList.fromList(items, Object::toString, items.get(0), false);
        return CaseDataBuilder.builder()
                .atStateClaimDraft()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .build()
                .toBuilder()
                .fastTrackMethodInPerson(localOptions)
                .disposalHearingMethodInPerson(localOptions)
                .smallClaimsMethodInPerson(localOptions)
                .build();
    }

    private CallbackParams buildCallbackParams(CaseData caseData) {
        return callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
    }

    @Test
    void shouldReturnNullDocument_whenInvokedAboutToSubmit() {
        CallbackParams params = buildCallbackParams(buildCaseData());
        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData()).doesNotContainKey("sdoOrderDocument");
    }
}
