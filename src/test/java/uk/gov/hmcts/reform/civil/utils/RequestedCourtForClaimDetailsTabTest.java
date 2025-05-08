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
        requestedCourt = RequestedCourt.builder()
            .responseCourtCode("121")
            .caseLocation(CaseLocationCivil.builder().baseLocation("00002").build())
            .reasonForHearingAtSpecificCourt("Close to home")
            .build();
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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(null)
                               .respondent1DQRemoteHearingLRspec(null)
                               .build());
        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1Spec(callbackParams, caseDataBuilder);
        RequestedCourtForTabDetails tabDetails = caseDataBuilder.build().getRequestedCourtForTabDetailsRes1();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo(null);
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo(null);
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo(null);
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(null);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo(null);
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForApplicantUnspec() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(requestedCourt)
                              .remoteHearing(RemoteHearing.builder()
                                                 .remoteHearingRequested(YES)
                                                 .reasonForRemoteHearing("Outside scares me")
                                                 .build())
                              .build());

        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicant(callbackParams, caseDataBuilder);
        RequestedCourtForTabDetails tabDetails = caseDataBuilder.build().getRequestedCourtForTabDetailsApp();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForApplicantSpec() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(requestedCourt)
                              .applicant1DQRemoteHearingLRspec(RemoteHearingLRspec.builder()
                                                                   .remoteHearingRequested(YES)
                                                                   .reasonForRemoteHearing("Outside scares me")
                                                                   .build())
                              .build());
        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicantSpec(callbackParams, caseDataBuilder);
        RequestedCourtForTabDetails tabDetails = caseDataBuilder.build().getRequestedCourtForTabDetailsApp();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent1() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(requestedCourt)
                               .respondent1DQRemoteHearing(RemoteHearing.builder()
                                                               .remoteHearingRequested(YES)
                                                               .reasonForRemoteHearing("Outside scares me")
                                                               .build())
                               .build());
        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1(callbackParams, caseDataBuilder);
        RequestedCourtForTabDetails tabDetails = caseDataBuilder.build().getRequestedCourtForTabDetailsRes1();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent2() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder()
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQRequestedCourt(requestedCourt)
                               .respondent2DQRemoteHearing(RemoteHearing.builder()
                                                               .remoteHearingRequested(YES)
                                                               .reasonForRemoteHearing("Outside scares me")
                                                               .build())
                               .build());
        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent2(callbackParams, caseDataBuilder);
        RequestedCourtForTabDetails tabDetails = caseDataBuilder.build().getRequestedCourtForTabDetailsRes2();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent1Spec() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(requestedCourt)
                               .respondent1DQRemoteHearingLRspec(RemoteHearingLRspec.builder()
                                                                     .remoteHearingRequested(YES)
                                                                     .reasonForRemoteHearing("Outside scares me")
                                                                     .build())
                               .build());
        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1Spec(callbackParams, caseDataBuilder);
        RequestedCourtForTabDetails tabDetails = caseDataBuilder.build().getRequestedCourtForTabDetailsRes1();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent2Spec() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder()
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQRequestedCourt(requestedCourt)
                               .respondent2DQRemoteHearingLRspec(RemoteHearingLRspec.builder()
                                                                     .remoteHearingRequested(YES)
                                                                     .reasonForRemoteHearing("Outside scares me")
                                                                     .build())
                               .build());
        CallbackParams callbackParams = CallbackParams.builder()
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "some-token"))
            .build();

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent2Spec(callbackParams, caseDataBuilder);
        RequestedCourtForTabDetails tabDetails = caseDataBuilder.build().getRequestedCourtForTabDetailsRes2();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getRequestedCourtName()).isEqualTo("court 2");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }
}
