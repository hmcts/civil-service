package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;
import static uk.gov.hmcts.reform.civil.service.GenAppStateHelperService.RequiredState.APPLICATION_CLOSED;
import static uk.gov.hmcts.reform.civil.service.GenAppStateHelperService.RequiredState.APPLICATION_PROCEEDS_OFFLINE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    GenAppStateHelperService.class, JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class GenAppStateHelperServiceTest {

    @Autowired
    private GenAppStateHelperService service;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    private static final String APPLICATION_CLOSED_TEXT = "Application Closed";
    private static final String APPLICATION_OFFLINE_TEXT = "Proceeds In Heritage";
    private static final String SET_DATE = "2022-08-31T22:50:11.2509019";

    @Nested
    class StatusChangeInApplicationDetailsInClaim {

        private void setupForApplicationClosed() {
            when(coreCaseDataService.getCase(1234L))
                    .thenReturn(getCaseDetails(1234L, "APPLICATION_CLOSED", APPLICATION_CLOSED));

            when(coreCaseDataService.getCase(2345L))
                    .thenReturn(getCaseDetails(2345L, "ORDER_MADE", APPLICATION_CLOSED));

            when(coreCaseDataService.getCase(3456L))
                    .thenReturn(getCaseDetails(3456L, "APPLICATION_CLOSED", APPLICATION_CLOSED));

            when(coreCaseDataService.getCase(4567L))
                    .thenReturn(getCaseDetails(4567L, "APPLICATION_CLOSED", APPLICATION_CLOSED));

            when(coreCaseDataService.getCase(5678L))
                    .thenReturn(getCaseDetails(5678L, "APPLICATION_CLOSED", APPLICATION_CLOSED));

            when(coreCaseDataService.getCase(6789L))
                    .thenReturn(getCaseDetails(6789L, "APPLICATION_CLOSED", APPLICATION_CLOSED));

            when(coreCaseDataService.getCase(7890L))
                    .thenReturn(getCaseDetails(7890L, "APPLICATION_DISMISSED", APPLICATION_CLOSED));

            when(coreCaseDataService.getCase(8910L))
                    .thenReturn(getCaseDetails(8910L, "PROCEEDS_IN_HERITAGE", APPLICATION_CLOSED));

            when(coreCaseDataService.getCase(1011L))
                    .thenReturn(getCaseDetails(1011L, "APPLICATION_CLOSED", APPLICATION_CLOSED));
        }

        private void setupForApplicationOffline() {
            when(coreCaseDataService.getCase(1234L))
                    .thenReturn(getCaseDetails(1234L, "PROCEEDS_IN_HERITAGE", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(2345L))
                    .thenReturn(getCaseDetails(2345L, "ORDER_MADE", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(3456L))
                    .thenReturn(getCaseDetails(3456L, "PROCEEDS_IN_HERITAGE", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(4567L))
                    .thenReturn(getCaseDetails(4567L, "PROCEEDS_IN_HERITAGE", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(5678L))
                    .thenReturn(getCaseDetails(5678L, "PROCEEDS_IN_HERITAGE", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(6789L))
                    .thenReturn(getCaseDetails(6789L, "PROCEEDS_IN_HERITAGE", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(7890L))
                    .thenReturn(getCaseDetails(7890L, "APPLICATION_DISMISSED", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(8910L))
                    .thenReturn(getCaseDetails(8910L, "PROCEEDS_IN_HERITAGE", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(1011L))
                    .thenReturn(getCaseDetails(1011L, "PROCEEDS_IN_HERITAGE", APPLICATION_PROCEEDS_OFFLINE));

            when(coreCaseDataService.getCase(1112L))
                    .thenReturn(getCaseDetails(1011L, "APPLICATION_CLOSED", APPLICATION_PROCEEDS_OFFLINE));
        }

        @Test
        public void updateApplicationDetailsListsToReflectLatestApplicationStatusChange_AC() {
            setupForApplicationClosed();
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseDataWithDetails(CaseData.builder().build(),
                            true,
                            true,
                            true,
                            getOriginalStatusOfGeneralApplication_applicationClosed());

            CaseData updatedData = service.updateApplicationDetailsInClaim(
                    caseData,
                    APPLICATION_CLOSED_TEXT,
                    APPLICATION_CLOSED);

            assertStatusChangeApplicationClosed(updatedData, "1234", true);
            assertStatusChangeApplicationClosed(updatedData, "2345", false);
            assertStatusChangeApplicationClosed(updatedData, "3456", true);
            assertStatusChangeApplicationClosed(updatedData, "4567", true);
            assertStatusChangeApplicationClosed(updatedData, "5678", true);
            assertStatusChangeApplicationClosed(updatedData, "6789", true);
            assertStatusChangeApplicationClosed(updatedData, "7890", false);
            assertStatusChangeApplicationClosed(updatedData, "8910", false);
            assertStatusChangeApplicationClosed(updatedData, "1011", true);
        }

        @Test
        public void noUpdatesToCaseDataIfThereAreNoGeneralApplications_AC() {
            setupForApplicationClosed();
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseDataWithDetails(CaseData.builder().build(),
                            false,
                            false,
                            false,
                            Map.of());

            CaseData response = service.updateApplicationDetailsInClaim(
                    caseData,
                    APPLICATION_CLOSED_TEXT,
                    APPLICATION_CLOSED);

            CaseData updatedData = mapper.convertValue(response, CaseData.class);

            assertThat(updatedData.getGeneralApplications()).isEmpty();
            assertThat(updatedData.getGeneralApplicationsDetails()).isNull();
            assertThat(updatedData.getGaDetailsRespondentSol()).isNull();
            verifyNoMoreInteractions(coreCaseDataService);
        }

        @Test
        public void noUpdateToApplicationDetailsListsWhenApplicationClosedDateNotSet() {
            when(coreCaseDataService.getCase(9999L))
                    .thenReturn(getCaseDetails(1234L, "PROCEEDS_IN_HERITAGE", APPLICATION_CLOSED));
            Map<String, String> applications = new HashMap<>();
            applications.put("9999", "Application Submitted - Awaiting Judicial Decision");
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseDataWithDetails(CaseData.builder().build(),
                            true,
                            true,
                            true,
                            applications);
            CaseData updatedData = service.updateApplicationDetailsInClaim(
                    caseData,
                    APPLICATION_CLOSED_TEXT,
                    APPLICATION_CLOSED);

            assertStatusChangeApplicationClosed(updatedData, "9999", false);
        }

        @Test
        public void updateApplicationDetailsListsToReflectLatestApplicationStatusChange_AO() {
            setupForApplicationOffline();
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseDataWithDetails(CaseData.builder().build(),
                            true,
                            true,
                            true,
                            getOriginalStatusOfGeneralApplication_applicationOffline());

            CaseData updatedData = service.updateApplicationDetailsInClaim(
                    caseData,
                    APPLICATION_OFFLINE_TEXT,
                    APPLICATION_PROCEEDS_OFFLINE);

            assertStatusChangeApplicationOffline(updatedData, "1234", true);
            assertStatusChangeApplicationOffline(updatedData, "2345", false);
            assertStatusChangeApplicationOffline(updatedData, "3456", true);
            assertStatusChangeApplicationOffline(updatedData, "4567", true);
            assertStatusChangeApplicationOffline(updatedData, "5678", true);
            assertStatusChangeApplicationOffline(updatedData, "6789", true);
            assertStatusChangeApplicationOffline(updatedData, "7890", false);
            assertStatusChangeApplicationOffline(updatedData, "8910", true);
            assertStatusChangeApplicationOffline(updatedData, "1011", true);
            assertStatusChangeApplicationOffline(updatedData, "1112", false);
        }

        @Test
        public void noUpdatesToCaseDataIfThereAreNoGeneralApplications_AO() {
            setupForApplicationOffline();
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseDataWithDetails(CaseData.builder().build(),
                            false,
                            false,
                            false,
                            Map.of());

            CaseData response = service.updateApplicationDetailsInClaim(
                    caseData,
                    APPLICATION_OFFLINE_TEXT,
                    APPLICATION_PROCEEDS_OFFLINE);

            CaseData updatedData = mapper.convertValue(response, CaseData.class);

            assertThat(updatedData.getGeneralApplications()).isEmpty();
            assertThat(updatedData.getGeneralApplicationsDetails()).isNull();
            assertThat(updatedData.getGaDetailsRespondentSol()).isNull();
            verifyNoMoreInteractions(coreCaseDataService);
        }

        @Test
        public void noUpdateToApplicationDetailsListsWhenApplicationOfflineDateNotSet() {
            when(coreCaseDataService.getCase(9999L))
                    .thenReturn(getCaseDetails(1234L, "PROCEEDS_IN_HERITAGE", APPLICATION_PROCEEDS_OFFLINE));
            Map<String, String> applications = new HashMap<>();
            applications.put("9999", "Application Submitted - Awaiting Judicial Decision");
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseDataWithDetails(CaseData.builder().build(),
                            true,
                            true,
                            true,
                            applications);
            CaseData updatedData = service.updateApplicationDetailsInClaim(
                    caseData,
                    APPLICATION_OFFLINE_TEXT,
                    APPLICATION_PROCEEDS_OFFLINE);

            assertStatusChangeApplicationClosed(updatedData, "9999", false);
        }

        private void assertStatusChangeApplicationClosed(CaseData updatedData, String childCaseRef,
                                                         boolean shouldApplicationBeInClosedState) {
            assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef)).isNotNull();
            assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef)).isNotNull();
            if (shouldApplicationBeInClosedState) {
                assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                        .isEqualTo(APPLICATION_CLOSED_TEXT);
                assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                        .isEqualTo(APPLICATION_CLOSED_TEXT);
            } else {
                assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                        .isNotEqualTo(APPLICATION_CLOSED_TEXT);
                assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                        .isNotEqualTo(APPLICATION_CLOSED_TEXT);
            }
        }

        private void assertStatusChangeApplicationOffline(CaseData updatedData, String childCaseRef,
                                                          boolean shouldApplicationBeInOfflineState) {
            assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef)).isNotNull();
            assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef)).isNotNull();
            if (shouldApplicationBeInOfflineState) {
                assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                        .isEqualTo(APPLICATION_OFFLINE_TEXT);
                assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                        .isEqualTo(APPLICATION_OFFLINE_TEXT);
            } else {
                assertThat(getGADetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                        .isNotEqualTo(APPLICATION_OFFLINE_TEXT);
                assertThat(getGARespDetailsFromUpdatedCaseData(updatedData, childCaseRef).getCaseState())
                        .isNotEqualTo(APPLICATION_OFFLINE_TEXT);
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

        private Map<String, String> getOriginalStatusOfGeneralApplication_applicationClosed() {
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

        private Map<String, String> getOriginalStatusOfGeneralApplication_applicationOffline() {
            Map<String, String> latestStatus = getOriginalStatusOfGeneralApplication_applicationClosed();
            latestStatus.put("1112", "Application Closed");

            return latestStatus;
        }

        private CaseDetails getCaseDetails(
                long ccdRef,
                String caseState,
                GenAppStateHelperService.RequiredState gaFlow) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("generalAppDetailsOfOrder", "Some Value");
            if (APPLICATION_CLOSED.equals(gaFlow)) {
                dataMap.put("applicationClosedDate", SET_DATE);
            }
            if (APPLICATION_PROCEEDS_OFFLINE.equals(gaFlow)) {
                dataMap.put("applicationTakenOfflineDate", SET_DATE);
            }

            CaseDetails.CaseDetailsBuilder builder = CaseDetails.builder();
            builder.data(dataMap);
            builder.id(ccdRef).state(caseState).build();

            return builder.build();
        }
    }

    @Nested
    class TriggerGenAppEvents {

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplication() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseDataWithDetails(CaseData.builder().build(),
                            true,
                            true,
                            true,
                            getOriginalStatusOfGeneralApplication());

            service.triggerEvent(caseData, MAIN_CASE_CLOSED);

            verify(coreCaseDataService, times(1)).triggerGeneralApplicationEvent(1234L, MAIN_CASE_CLOSED);
            verify(coreCaseDataService, times(1)).triggerGeneralApplicationEvent(2345L, MAIN_CASE_CLOSED);
            verifyNoMoreInteractions(coreCaseDataService);
        }

        @Test
        void shouldNotTriggerGeneralApplicationEvent_whenCaseHasNoGeneralApplication() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();

            service.triggerEvent(caseData, MAIN_CASE_CLOSED);

            verifyNoInteractions(coreCaseDataService);
        }

        private Map<String, String> getOriginalStatusOfGeneralApplication() {
            Map<String, String> latestStatus = new HashMap<>();
            latestStatus.put("1234", "Application Submitted - Awaiting Judicial Decision");
            latestStatus.put("2345", "Order Made");
            return latestStatus;
        }
    }

}
