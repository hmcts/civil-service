package uk.gov.hmcts.reform.civil.helpers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;

import java.util.List;
import java.util.Optional;

public class LocationHelperTest {

    private final LocationHelper helper = new LocationHelper(ccmccAmount, ccmccRegionId, ccmccEpimsId);

    @Test
    public void thereIsAMatchingLocation() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        List<LocationRefData> locations = List.of(LocationRefData.builder()
                                                      .courtLocationCode("123")
                                                      .regionId("regionId")
                                                      .region("region name")
                                                      .epimmsId("epimms")
                                                      .build());
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("123")
            .requestHearingAtSpecificCourt(YesOrNo.YES)
            .build();
        helper.updateCaseManagementLocation(updatedData, requestedCourt, () -> locations);
        Assertions.assertThat(updatedData.build().getCaseManagementLocation())
            .isNotNull()
            .isEqualTo(CaseLocation.builder()
                           .region("regionId")
                           .baseLocation("epimms")
                           .build());
    }

    @Test
    public void whenSpecDefendantIsPerson_courtIsDefendants() {
        CaseData caseData = CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
                                      .requestHearingAtSpecificCourt(YesOrNo.YES)
                                      .responseCourtCode("123")
                                      .build()
                              )
                              .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .requestHearingAtSpecificCourt(YesOrNo.YES)
                                       .responseCourtCode("321")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.isPresent()).isTrue();
        Assertions.assertThat(court.get()).isEqualTo(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt());
    }

    @Test
    public void whenDefendantIsPerson_courtIsDefendants() {
        CaseData caseData = CaseData.builder()
            .superClaimType(SuperClaimType.UNSPEC_CLAIM)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(CourtLocation.builder()
                               .applicantPreferredCourt("123")
                               .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .requestHearingAtSpecificCourt(YesOrNo.YES)
                                       .responseCourtCode("321")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.isPresent()).isTrue();
        Assertions.assertThat(court.get()).isEqualTo(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt());
    }

    @Test
    public void whenSpecDefendantIsGroup_courtIsClaimants() {
        CaseData caseData = CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
                                      .requestHearingAtSpecificCourt(YesOrNo.YES)
                                      .responseCourtCode("123")
                                      .build()
                              )
                              .build())
            .respondent1(Party.builder()
                             .type(Party.Type.ORGANISATION)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .requestHearingAtSpecificCourt(YesOrNo.YES)
                                       .responseCourtCode("321")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.isPresent()).isTrue();
        Assertions.assertThat(court.get())
            .isEqualTo(caseData.getApplicant1DQ().getApplicant1DQRequestedCourt());
    }

    @Test
    public void whenDefendantIsGroup_courtIsClaimants() {
        CaseData caseData = CaseData.builder()
            .superClaimType(SuperClaimType.UNSPEC_CLAIM)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(CourtLocation.builder()
                               .applicantPreferredCourt("123")
                               .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .requestHearingAtSpecificCourt(YesOrNo.YES)
                                       .responseCourtCode("321")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.isPresent()).isTrue();
        Assertions.assertThat(court.get().getResponseCourtCode())
            .isEqualTo(caseData.getCourtLocation().getApplicantPreferredCourt());
    }
}
