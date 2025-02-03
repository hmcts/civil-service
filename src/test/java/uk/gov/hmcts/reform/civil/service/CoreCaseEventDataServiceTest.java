package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCaseEventDataServiceTest {

    @InjectMocks
    private CoreCaseEventDataService service;
    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private CaseEventsApi caseEventsApi;
    @Mock
    private UserService userService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final String CASE_TYPE = "CIVIL";

    @BeforeEach
    void init() {
        clearInvocations(authTokenGenerator, userService);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Nested
    class TriggerEvent {

        private static final String JURISDICTION = "CIVIL";
        private static final String CASE_ID = "1";
        private static final String USER_ID = "User1";

        @BeforeEach
        void setUp() {
            when(userService.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(USER_AUTH_TOKEN);
            when(caseEventsApi.findEventDetailsForCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID))
                .thenReturn(buildCaseEventDetails());
        }

        @Test
        void shouldStartAndSubmitEvent_WhenCalled() {
            var a = service.getEventsForCase(CASE_ID);
            assertThat(a.stream().map(CaseEventDetail::getId).toList()).contains(CaseEvent.TRANSFER_ONLINE_CASE.name());
        }

        private List<CaseEventDetail> buildCaseEventDetails() {
            return List.of(
                CaseEventDetail.builder()
                    .userId("system user id")
                    .userLastName("System-update")
                    .userFirstName("system email")
                    .createdDate(LocalDateTime.now().minusHours(2))
                    .caseTypeId("CIVIL")
                    .caseTypeVersion(1)
                    .description(null)
                    .eventName("Start business process")
                    .id("START_BUSINESS_PROCESS")
                    .stateId("AWAITING_APPLICANT_INTENTION")
                    .stateName("Claimant Intent Pending")
                    .data(null)
                    .dataClassification(null)
                    .significantItem(null)
                    .build(),
                CaseEventDetail.builder()
                    .userId("claimant user id")
                    .userLastName("Claimant-solicitor")
                    .userFirstName("claimant email")
                    .createdDate(LocalDateTime.now().minusHours(1))
                    .caseTypeId("CIVIL")
                    .caseTypeVersion(1)
                    .description("")
                    .eventName("Transfer online case")
                    .id("TRANSFER_ONLINE_CASE")
                    .stateId("AWAITING_APPLICANT_INTENTION")
                    .stateName("Claimant Intent Pending")
                    .data(null)
                    .dataClassification(null)
                    .significantItem(null)
                    .build()
            );
        }
    }
}
