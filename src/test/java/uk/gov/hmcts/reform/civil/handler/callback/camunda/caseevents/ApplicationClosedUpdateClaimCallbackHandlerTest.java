package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_CLOSED_UPDATE_CLAIM;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    ApplicationClosedUpdateClaimCallbackHandler.class, JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class ApplicationClosedUpdateClaimCallbackHandlerTest extends BaseCallbackHandlerTest {
    @Autowired
    private ApplicationClosedUpdateClaimCallbackHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private FeatureToggleService featureToggle;

    private static final String APPLICATION_CLOSED = "Application Closed";

    @BeforeEach
    void prepare() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        when(coreCaseDataService.getCase(1234L)).thenReturn(getCaseDetails(1234L, "APPLICATION_CLOSED", true));
        when(coreCaseDataService.getCase(2345L)).thenReturn(getCaseDetails(2345L, "ORDER_MADE", true));
        when(coreCaseDataService.getCase(3456L)).thenReturn(getCaseDetails(3456L, "APPLICATION_CLOSED", true));
        when(coreCaseDataService.getCase(4567L)).thenReturn(getCaseDetails(4567L, "APPLICATION_CLOSED", true));
        when(coreCaseDataService.getCase(5678L)).thenReturn(getCaseDetails(5678L, "APPLICATION_CLOSED", true));
        when(coreCaseDataService.getCase(6789L)).thenReturn(getCaseDetails(6789L, "APPLICATION_CLOSED", true));
        when(coreCaseDataService.getCase(7890L)).thenReturn(getCaseDetails(7890L, "APPLICATION_DISMISSED", true));
        when(coreCaseDataService.getCase(8910L)).thenReturn(getCaseDetails(8910L, "PROCEEDS_IN_HERITAGE", true));
        when(coreCaseDataService.getCase(1011L)).thenReturn(getCaseDetails(1011L, "APPLICATION_CLOSED", true));
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(APPLICATION_CLOSED_UPDATE_CLAIM);
    }

    @Test
    void shouldReturnError_whenGeneralApplicationToggledOff() {
        CaseData caseData = CaseDataBuilder.builder()
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

        when(featureToggle.isGeneralApplicationsEnabled()).thenReturn(false);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors())
                .containsOnly("Invalid request since the general application feature is toggled off");
    }

    @Test
    void shouldNotReturnError_whenGeneralApplicationToggledOn() {
        CaseData caseData = CaseDataBuilder.builder()
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

        when(featureToggle.isGeneralApplicationsEnabled()).thenReturn(true);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    public void updateApplicationDetailsListsToReflectLatestApplicationStatusChange() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        true,
                        true,
                        true,
                        getOriginalStatusOfGeneralApplication_test1());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertStatusChange(updatedData, "1234", true);
        assertStatusChange(updatedData, "2345", false);
        assertStatusChange(updatedData, "3456", true);
        assertStatusChange(updatedData, "4567", true);
        assertStatusChange(updatedData, "5678", true);
        assertStatusChange(updatedData, "6789", true);
        assertStatusChange(updatedData, "7890", false);
        assertStatusChange(updatedData, "8910", false);
        assertStatusChange(updatedData, "1011", true);
    }

    @Test
    public void noUpdatesToCaseDataIfThereAreNoGeneralApplications() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        false,
                        false,
                        false,
                        Map.of());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(response.getErrors()).isNull();
        assertThat(updatedData.getGeneralApplications()).isEmpty();
        assertThat(updatedData.getGeneralApplicationsDetails()).isNull();
        assertThat(updatedData.getGaDetailsRespondentSol()).isNull();
        assertThat(updatedData.getGaDetailsRespondentSolTwo()).isNull();
        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    public void noUpdateToApplicationDetailsListsWhenApplicationClosedDateNotSet() {
        Map<String, String> applications = new HashMap<>();
        applications.put("9999", "Application Submitted - Awaiting Judicial Decision");
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        true,
                        true,
                        true,
                        applications);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(coreCaseDataService.getCase(9999L)).thenReturn(getCaseDetails(1234L, "PROCEEDS_IN_HERITAGE", false));

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertStatusChange(updatedData, "9999", false);
    }

    private void assertStatusChange(CaseData updatedData, String childCaseRef,
                                    boolean shouldApplicationBeInClosedState) {
        assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef)).isNotNull();
        assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef)).isNotNull();
        assertThat(getGARespTwoDetailsFromUpdatedCaseData(updatedData, childCaseRef)).isNotNull();
        if (shouldApplicationBeInClosedState) {
            assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                    .isEqualTo(APPLICATION_CLOSED);
            assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                    .isEqualTo(APPLICATION_CLOSED);
            assertThat(getGARespTwoDetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                    .isEqualTo(APPLICATION_CLOSED);
        } else {
            assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                    .isNotEqualTo(APPLICATION_CLOSED);
            assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                    .isNotEqualTo(APPLICATION_CLOSED);
            assertThat(getGARespTwoDetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                    .isNotEqualTo(APPLICATION_CLOSED);
        }
    }

    private GeneralApplicationsDetails getGADetailsFromUpdatedCaseData(CaseData caseData,
                                                                       String gaCaseRef) {
        Optional<Element<GeneralApplicationsDetails>> first = caseData.getGeneralApplicationsDetails().stream()
                .filter(ga -> gaCaseRef.equals(ga.getValue().getCaseLink().getCaseReference())).findFirst();
        return first.map(Element::getValue).orElse(null);
    }

    private GADetailsRespondentSol getGARespDetailsFromUpdatedCaseData(CaseData caseData,
                                                                       String gaCaseRef) {
        Optional<Element<GADetailsRespondentSol>> first = caseData.getGaDetailsRespondentSol().stream()
                .filter(ga -> gaCaseRef.equals(ga.getValue().getCaseLink().getCaseReference())).findFirst();
        return first.map(Element::getValue).orElse(null);
    }

    private GADetailsRespondentSol getGARespTwoDetailsFromUpdatedCaseData(CaseData caseData,
                                                                          String gaCaseRef) {
        Optional<Element<GADetailsRespondentSol>> first = caseData.getGaDetailsRespondentSolTwo().stream()
                .filter(ga -> gaCaseRef.equals(ga.getValue().getCaseLink().getCaseReference())).findFirst();
        return first.map(Element::getValue).orElse(null);
    }

    private Map<String, String> getOriginalStatusOfGeneralApplication_test1() {
        Map<String, String> latestStatus = new HashMap<>();
        latestStatus.put("1234", "Application Submitted - Awaiting Judicial Decision");
        latestStatus.put("2345", "Order Made");
        latestStatus.put("3456", "Awaiting Respondent Response");
        latestStatus.put("4567", "Directions Order Made");
        latestStatus.put("5678", "Awaiting Written Representations");
        latestStatus.put("6789", "Additional Information Require");
        latestStatus.put("7890", "Application Dismissed");
        latestStatus.put("8910", "Proceeds In Heritage");
        latestStatus.put("1011", "Listed for a Hearing");

        return latestStatus;
    }

    private CaseDetails getCaseDetails(long ccdRef, String caseState, boolean setDate) {
        CaseDetails.CaseDetailsBuilder builder = CaseDetails.builder();
        if (setDate) {
            builder.data(Map.of("applicationClosedDate", "2022-08-31T22:50:11.2509019"));
        } else {
            builder.data(Map.of("generalAppDetailsOfOrder", "Some Value"));
        }
        builder.id(ccdRef).state(caseState).build();
        return builder.build();
    }
}

