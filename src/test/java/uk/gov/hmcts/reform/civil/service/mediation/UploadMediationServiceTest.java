package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class UploadMediationServiceTest {

    public static final CaseData CASE_DATA = CaseData.builder()
        .legacyCaseReference("reference")
        .ccdCaseReference(1234L)
        .build();
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private UserService userService;
    @Mock
    private  FeatureToggleService featureToggleService;

    @InjectMocks
    private UploadMediationService uploadMediationService;

    HashMap<String, Object> params = new HashMap<>();

    @BeforeEach
    void setUp() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
    }

    @Test
    void shouldReturnRecordScenarioWhenCarmIsEnabledAndApplicantSolicitorOne() {
        //Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));

        var callBack = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, CASE_DATA).build();
        //When
        uploadMediationService.uploadMediationDocumentsTaskList(callBack);
        //Then
        verify(dashboardApiClient, times(2)).recordScenario(any(), any(), any(), any());

    }

    @Test
    void shouldReturnRecordScenarioWhenCarmIsEnabledAndClaimant() {
        //Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

        var callBack = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, CASE_DATA).build();
        //When
        uploadMediationService.uploadMediationDocumentsTaskList(callBack);
        //Then
        verify(dashboardApiClient, times(2)).recordScenario(any(), any(), any(), any());

    }

    @Test
    void shouldReturnRecordScenarioWhenCarmIsEnabledAndRespondentSolicitorOne() {
        //Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));

        var callBack = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, CASE_DATA).build();
        //When
        uploadMediationService.uploadMediationDocumentsTaskList(callBack);
        //Then
        verify(dashboardApiClient, times(2)).recordScenario(any(), any(), any(), any());

    }

    @Test
    void shouldReturnRecordScenarioWhenCarmIsEnabledAndDefendant() {
        //Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));

        var callBack = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, CASE_DATA).build();
        //When
        uploadMediationService.uploadMediationDocumentsTaskList(callBack);
        //Then
        verify(dashboardApiClient, times(2)).recordScenario(any(), any(), any(), any());

    }

    @Test
    void shouldReturnRecordScenarioWhenCarmIsEnabledandRespondentSolicitorOne() {
        //Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()));

        var callBack = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, CASE_DATA).build();
        //When
        uploadMediationService.uploadMediationDocumentsTaskList(callBack);
        //Then
        verify(dashboardApiClient, times(0)).recordScenario(any(), any(), any(), any());

    }

    @Test
    void shouldReturnRecordScenarioWhenCarmIsNotEnabled() {
        //Given
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        var callBack = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, CASE_DATA).build();
        //When
        uploadMediationService.uploadMediationDocumentsTaskList(callBack);
        //Then
        verify(dashboardApiClient, times(0)).recordScenario(any(), any(), any(), any());

    }

}

