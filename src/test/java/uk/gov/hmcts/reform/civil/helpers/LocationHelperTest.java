package uk.gov.hmcts.reform.civil.helpers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public class LocationHelperTest {

    private static final BigDecimal CCMCC_AMOUNT = BigDecimal.valueOf(1000);
    private static final String CCMCC_REGION_ID = "ccmccRegionId";
    private static final String CCMCC_EPIMS = "ccmccEpims";
    private final LocationHelper helper = new LocationHelper(CCMCC_AMOUNT, CCMCC_EPIMS, CCMCC_REGION_ID);

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
            .build();
        helper.updateCaseManagementLocation(updatedData, requestedCourt, () -> locations);
        Assertions.assertThat(updatedData.build().getCaseManagementLocation())
            .isNotNull()
            .isEqualTo(CaseLocationCivil.builder()
                           .region("regionId")
                           .baseLocation("epimms")
                           .build());
    }

    @Test
    public void whenSpecDefendantIsPerson_courtIsDefendants() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
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
    public void whenSpecDefendantIsPersonAndDefendant2_courtIsDefendant2() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
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
                                       .responseCourtCode("321")
                                       .build()
                               )
                               .build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQRequestedCourt(RequestedCourt.builder()
                                                                .responseCourtCode("432")
                                                                .build())
                               .build())
            .respondent2ResponseDate(LocalDateTime.now())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.isPresent()).isTrue();
        Assertions.assertThat(court.get()).isEqualTo(caseData.getRespondent2DQ().getRespondent2DQRequestedCourt());
    }

    @Test
    public void whenLessThan1000_locationIsCcmcc() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                            .build())
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(CourtLocation.builder()
                               .applicantPreferredCourt("123")
                               .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
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
                                       .responseCourtCode("321")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.isPresent()).isTrue();
        Assertions.assertThat(court.orElseThrow().getResponseCourtCode())
            .isEqualTo(caseData.getCourtLocation().getApplicantPreferredCourt());

    }

    @Test
    public void whenLessThan1000_locationIsCcmccEvenUndef() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.isPresent()).isTrue();
        Assertions.assertThat(court.get().getCaseLocation())
            .isEqualTo(CaseLocationCivil.builder()
                           .baseLocation(CCMCC_EPIMS)
                           .region(CCMCC_REGION_ID).build());
    }

    @Test
    public void whenDefendantIsPerson_courtIsDefendants() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(10000_00))
                            .build())
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
                                       .responseCourtCode("321")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.isPresent()).isTrue();
        Assertions.assertThat(court.orElseThrow().getResponseCourtCode())
            .isEqualTo(caseData.getCourtLocation().getApplicantPreferredCourt());
    }

    @Test
    public void whenSpecDefendantIsGroup_courtIsClaimants() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
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
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(10000_00))
                            .build())
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

    @Test
    public void when1v2AnyIndividual_thenCourtIsIndividualDefendant() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(10000_00))
                            .build())
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
                                       .responseCourtCode("321")
                                       .build()
                               )
                               .build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("432")
                                       .build()
                               )
                               .build())
            // company answered first
            .respondent1ResponseDate(LocalDateTime.now().minusDays(2))
            .respondent2ResponseDate(LocalDateTime.now())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);

        Assertions.assertThat(court.orElseThrow().getResponseCourtCode())
            .isEqualTo(caseData.getCourtLocation().getApplicantPreferredCourt());
    }
}
