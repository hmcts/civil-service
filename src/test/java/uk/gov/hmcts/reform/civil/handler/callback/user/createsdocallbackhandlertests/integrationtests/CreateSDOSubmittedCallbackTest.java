package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_HEADER_SDO;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_1_V_1;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_1_V_2;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_2_V_1;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.FEEDBACK_LINK;

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
public class CreateSDOSubmittedCallbackTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000DC001";

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

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @Autowired
    private CreateSDOCallbackHandler handler;

    private SubmittedCallbackResponse generateExpectedResponse(String summary, Object... args) {
        String header = format(CONFIRMATION_HEADER_SDO, REFERENCE_NUMBER);
        String body = format(summary, args) + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");
        return SubmittedCallbackResponse.builder()
                .confirmationHeader(header)
                .confirmationBody(body)
                .build();
    }

    @Test
    void shouldReturnExpectedSubmittedCallbackResponse_1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
        SubmittedCallbackResponse expected = generateExpectedResponse(CONFIRMATION_SUMMARY_1_V_1, "Mr. John Rambo", "Mr. Sole Trader");
        assertThat(response).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedSubmittedCallbackResponse_1v2() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().multiPartyClaimTwoDefendantSolicitors().build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
        SubmittedCallbackResponse expected = generateExpectedResponse(CONFIRMATION_SUMMARY_1_V_2, "Mr. John Rambo", "Mr. Sole Trader", "Mr. John Rambo");
        assertThat(response).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedSubmittedCallbackResponse_2v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().multiPartyClaimTwoApplicants().build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
        SubmittedCallbackResponse expected = generateExpectedResponse(CONFIRMATION_SUMMARY_2_V_1, "Mr. John Rambo", "Mr. Jason Rambo", "Mr. Sole Trader");
        assertThat(response).usingRecursiveComparison().isEqualTo(expected);
    }
}
