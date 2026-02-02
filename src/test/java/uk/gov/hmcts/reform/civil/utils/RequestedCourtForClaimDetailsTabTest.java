package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RequestedCourtForTabDetails;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearing;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearingLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestedCourtForClaimDetailsTabTest {

    @Mock
    LocationReferenceDataService locationRefDataService;
    @InjectMocks
    private RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;

    private RequestedCourt requestedCourt;
    private List<LocationRefData> locations;

    @BeforeEach
    void setUp() {
        requestedCourt = new RequestedCourt()
            .setResponseCourtCode("121")
            .setCaseLocation(new CaseLocationCivil().setBaseLocation("00002"))
            .setReasonForHearingAtSpecificCourt("Close to home");
        locations = List.of(
            LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
            LocationRefData.builder().epimmsId("00002").courtLocationCode("00002")
                .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
            LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
        );
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(locations);
    }

    @Test
    void shouldNotUpdateRequestedCourtDetailsWHenRequestedCourtNull() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setRespondent1DQ(
            new Respondent1DQ()
                .setRespondent1DQRequestedCourt(null)
                .setRespondent1DQRemoteHearingLRspec(null)
        );

        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        // When
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1Spec(caseData, callbackParams);

        // Then
        RequestedCourtForTabDetails tabDetails = caseData.getRequestedCourtForTabDetailsRes1();
        assertThat(tabDetails.getRequestedCourt()).isEqualTo(null);
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo(null);
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo(null);
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(null);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo(null);
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForApplicantUnspec() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        caseData.setApplicant1DQ(
            new Applicant1DQ()
                .setApplicant1DQRequestedCourt(requestedCourt)
                .setRemoteHearing(new RemoteHearing()
                                      .setRemoteHearingRequested(YES)
                                      .setReasonForRemoteHearing("Outside scares me"))
        );

        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        // When
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicant(callbackParams, caseData);

        // Then
        RequestedCourtForTabDetails tabDetails = caseData.getRequestedCourtForTabDetailsApp();
        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForApplicantSpec() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setApplicant1DQ(
            new Applicant1DQ()
                .setApplicant1DQRequestedCourt(requestedCourt)
                .setApplicant1DQRemoteHearingLRspec(new RemoteHearingLRspec()
                                                        .setRemoteHearingRequested(YES)
                                                        .setReasonForRemoteHearing("Outside scares me"))
        );

        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        // When
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicantSpec(callbackParams, caseData);

        // Then
        RequestedCourtForTabDetails tabDetails = caseData.getRequestedCourtForTabDetailsApp();
        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent1() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setRespondent1DQ(
            new Respondent1DQ()
                .setRespondent1DQRequestedCourt(requestedCourt)
                .setRespondent1DQRemoteHearing(new RemoteHearing()
                                                   .setRemoteHearingRequested(YES)
                                                   .setReasonForRemoteHearing("Outside scares me"))
        );

        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        // When
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1(callbackParams, caseData);

        // Then
        RequestedCourtForTabDetails tabDetails = caseData.getRequestedCourtForTabDetailsRes1();
        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent2() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setRespondent2DQ(
            new Respondent2DQ()
                .setRespondent2DQRequestedCourt(requestedCourt)
                .setRespondent2DQRemoteHearing(new RemoteHearing()
                                                   .setRemoteHearingRequested(YES)
                                                   .setReasonForRemoteHearing("Outside scares me"))
        );

        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        // When
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent2(callbackParams, caseData);

        // Then
        RequestedCourtForTabDetails tabDetails = caseData.getRequestedCourtForTabDetailsRes2();
        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent1Spec() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setRespondent1DQ(
            new Respondent1DQ()
                .setRespondent1DQRequestedCourt(requestedCourt)
                .setRespondent1DQRemoteHearingLRspec(new RemoteHearingLRspec()
                                                         .setRemoteHearingRequested(YES)
                                                         .setReasonForRemoteHearing("Outside scares me"))
        );

        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        // When
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1Spec(caseData, callbackParams);

        // Then
        RequestedCourtForTabDetails tabDetails = caseData.getRequestedCourtForTabDetailsRes1();
        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent2Spec() {
        // Given
        CaseData caseData = CaseData.builder().build();
        caseData.setRespondent2DQ(
            new Respondent2DQ()
                .setRespondent2DQRequestedCourt(requestedCourt)
                .setRespondent2DQRemoteHearingLRspec(new RemoteHearingLRspec()
                                                         .setRemoteHearingRequested(YES)
                                                         .setReasonForRemoteHearing("Outside scares me"))
        );

        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        // When
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent2Spec(callbackParams, caseData);

        // Then
        RequestedCourtForTabDetails tabDetails = caseData.getRequestedCourtForTabDetailsRes2();
        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }
}
