package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RequestedCourtForTabDetails;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearing;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearingLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class RequestedCourtForClaimDetailsTabTest {

    @Test
    void shouldUpdateRequestedCourtTabDetailsForApplicantUnspec() {
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("121")
            .reasonForHearingAtSpecificCourt("Close to home")
            .build();

        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(requestedCourt)
                              .remoteHearing(RemoteHearing.builder()
                                                 .remoteHearingRequested(YES)
                                                 .reasonForRemoteHearing("Outside scares me")
                                                 .build())
                              .build());

        RequestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicant(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        RequestedCourtForTabDetails tabDetails = updatedCaseData.getRequestedCourtForTabDetailsApp();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForApplicantSpec() {
        RequestedCourt requestedCourt = RequestedCourt.builder()
                .responseCourtCode("121")
                .reasonForHearingAtSpecificCourt("Close to home")
                .build();

        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .applicant1DQ(Applicant1DQ.builder()
                        .applicant1DQRequestedCourt(requestedCourt)
                        .applicant1DQRemoteHearingLRspec(RemoteHearingLRspec.builder()
                                .remoteHearingRequested(YES)
                                .reasonForRemoteHearing("Outside scares me")
                                .build())
                        .build());

        RequestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicantSpec(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        RequestedCourtForTabDetails tabDetails = updatedCaseData.getRequestedCourtForTabDetailsApp();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent1() {
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("121")
            .reasonForHearingAtSpecificCourt("Close to home")
            .build();

        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder()
            .respondent1DQ(Respondent1DQ.builder()
                              .respondent1DQRequestedCourt(requestedCourt)
                              .respondent1DQRemoteHearing(RemoteHearing.builder()
                                                 .remoteHearingRequested(YES)
                                                 .reasonForRemoteHearing("Outside scares me")
                                                 .build())
                              .build());

        RequestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        RequestedCourtForTabDetails tabDetails = updatedCaseData.getRequestedCourtForTabDetailsRes1();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent2() {
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("121")
            .reasonForHearingAtSpecificCourt("Close to home")
            .build();

        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder()
            .respondent2DQ(Respondent2DQ.builder()
                              .respondent2DQRequestedCourt(requestedCourt)
                              .respondent2DQRemoteHearing(RemoteHearing.builder()
                                                 .remoteHearingRequested(YES)
                                                 .reasonForRemoteHearing("Outside scares me")
                                                 .build())
                              .build());

        RequestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent2(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        RequestedCourtForTabDetails tabDetails = updatedCaseData.getRequestedCourtForTabDetailsRes2();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent1Spec() {
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("121")
            .reasonForHearingAtSpecificCourt("Close to home")
            .build();

        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(requestedCourt)
                               .respondent1DQRemoteHearingLRspec(RemoteHearingLRspec.builder()
                                                               .remoteHearingRequested(YES)
                                                               .reasonForRemoteHearing("Outside scares me")
                                                               .build())
                               .build());

        RequestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1Spec(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        RequestedCourtForTabDetails tabDetails = updatedCaseData.getRequestedCourtForTabDetailsRes1();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

    @Test
    void shouldUpdateRequestedCourtTabDetailsForRespondent2Spec() {
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("121")
            .reasonForHearingAtSpecificCourt("Close to home")
            .build();

        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder()
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQRequestedCourt(requestedCourt)
                               .respondent2DQRemoteHearingLRspec(RemoteHearingLRspec.builder()
                                                               .remoteHearingRequested(YES)
                                                               .reasonForRemoteHearing("Outside scares me")
                                                               .build())
                               .build());

        RequestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent2Spec(caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();
        RequestedCourtForTabDetails tabDetails = updatedCaseData.getRequestedCourtForTabDetailsRes2();

        assertThat(tabDetails.getRequestedCourt()).isEqualTo("121");
        assertThat(tabDetails.getReasonForHearingAtSpecificCourt()).isEqualTo("Close to home");
        assertThat(tabDetails.getRequestHearingHeldRemotely()).isEqualTo(YES);
        assertThat(tabDetails.getRequestHearingHeldRemotelyReason()).isEqualTo("Outside scares me");
    }

}
