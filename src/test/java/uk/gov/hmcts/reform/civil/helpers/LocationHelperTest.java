package uk.gov.hmcts.reform.civil.helpers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@ExtendWith(MockitoExtension.class)
class LocationHelperTest {

    private static final BigDecimal CCMCC_AMOUNT = BigDecimal.valueOf(1000);
    private static final String CCMCC_REGION_ID = "ccmccRegionId";
    private static final String CCMCC_EPIMS = "ccmccEpims";
    private static final String CNBC_EPIMS = "cnbcEpims";
    private static final String CNBC_REGION_ID = "cnbcRegionId";
    @Mock
    private FeatureToggleService featureToggleService;
    private LocationHelper helper;
    private final CaseLocationCivil claimantPreferredCourt = CaseLocationCivil.builder()
        .baseLocation("123456").region("region 1").build();
    private final CaseLocationCivil defendant1PreferredCourt = CaseLocationCivil.builder()
        .baseLocation("987456").region("region 1").build();
    private final CaseLocationCivil defendant2PreferredCourt = CaseLocationCivil.builder()
        .baseLocation("101010").region("region 3").build();

    @BeforeEach
    void setup() {
        helper = new LocationHelper(CCMCC_AMOUNT, CCMCC_EPIMS, CCMCC_REGION_ID, CNBC_EPIMS, CNBC_REGION_ID, featureToggleService);
    }

    @Test
    void thereIsAMatchingLocation() {
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
    void whenSpecDefendantIsPerson_courtIsDefendantsPreferred() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
                                      .caseLocation(claimantPreferredCourt)
                                      .build()
                              )
                              .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .caseLocation(defendant1PreferredCourt)
                                       .responseCourtCode("123")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(defendant1PreferredCourt);
    }

    @Test
    void whenLessThan1000AndSpecifiedClaim_locationIsCcmcc() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                            .build())
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
                                      .caseLocation(claimantPreferredCourt)
                                      .build()
                              )
                              .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .caseLocation(defendant1PreferredCourt)
                                       .responseCourtCode("123")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation())
            .isEqualTo(CaseLocationCivil.builder().baseLocation(CCMCC_EPIMS).region(CCMCC_REGION_ID).build());
    }

    @Test
    void whenLessThan1000AndDuringSdo_locationIsPreferredLocation() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(999))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
                                      .caseLocation(claimantPreferredCourt)
                                      .build()
                              )
                              .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .caseLocation(defendant1PreferredCourt)
                                       .responseCourtCode("123")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData, true);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(defendant1PreferredCourt);
    }

    @Test
    void whenDefendantIsPerson_courtIsClaimantsPreferredAsUnspec() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(10000_00))
                            .build())
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(CourtLocation.builder()
                               .caseLocation(claimantPreferredCourt)
                               .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .caseLocation(defendant1PreferredCourt)
                                       .responseCourtCode("123")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(claimantPreferredCourt);
    }

    @Test
    void whenSpecDefendantIsGroup_courtIsClaimantsPreferred() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
                                      .caseLocation(claimantPreferredCourt)
                                      .build()
                              )
                              .build())
            .respondent1(Party.builder()
                             .type(Party.Type.ORGANISATION)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .caseLocation(defendant1PreferredCourt)
                                       .responseCourtCode("123")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(claimantPreferredCourt);
    }

    @ParameterizedTest
    @CsvSource({
        "INTERMEDIATE_CLAIM, NO, cnbcEpims, cnbcRegionId",
        "MULTI_CLAIM, NO, cnbcEpims, cnbcRegionId",
        "INTERMEDIATE_CLAIM, YES, 123456, region 1",
        "MULTI_CLAIM, YES, 123456, region 1"
    })
    void whenSpecMultiOrIntermediateAndLip_courtIsCnbc(String claimTrack, String represented, String epimm, String region) {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
                                      .caseLocation(claimantPreferredCourt)
                                      .build()
                              )
                              .build())
            .applicant1Represented(YesOrNo.YES)
            .respondent1(Party.builder()
                             .type(Party.Type.ORGANISATION)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .caseLocation(defendant1PreferredCourt)
                                       .responseCourtCode("123")
                                       .build()
                               )
                               .build())
            .respondent1Represented(YesOrNo.valueOf(represented))
            .responseClaimTrack(claimTrack)
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation())
            .isEqualTo(CaseLocationCivil.builder().baseLocation(epimm).region(region).build());
    }

    @ParameterizedTest
    @CsvSource({
        "INTERMEDIATE_CLAIM, NO, cnbcEpims, cnbcRegionId",
        "MULTI_CLAIM, NO, cnbcEpims, cnbcRegionId",
        "INTERMEDIATE_CLAIM, YES, 123456, region 1",
        "MULTI_CLAIM, YES, 123456, region 1"
    })
    void whenUnSpecMultiOrIntermediateAndLip_courtIsCnbc(String claimTrack, String represented, String epimm, String region) {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(1000000_00))
                            .build())
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(CourtLocation.builder()
                               .caseLocation(claimantPreferredCourt)
                               .build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(
                                  RequestedCourt.builder()
                                      .caseLocation(claimantPreferredCourt)
                                      .build()
                              )
                              .build())
            .applicant1Represented(YesOrNo.YES)
            .respondent1(Party.builder()
                             .type(Party.Type.ORGANISATION)
                             .build())
            .respondent1Represented(YesOrNo.valueOf(represented))
            .allocatedTrack(AllocatedTrack.valueOf(claimTrack))
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation())
            .isEqualTo(CaseLocationCivil.builder().baseLocation(epimm).region(region).build());
    }

    @Test
    void whenUnspecDefendantIsGroup_courtIsClaimantsPreferred() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(10000_00))
                            .build())
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(CourtLocation.builder()
                               .caseLocation(claimantPreferredCourt)
                               .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .caseLocation(defendant1PreferredCourt)
                                       .responseCourtCode("123")
                                       .build()
                               )
                               .build())
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(claimantPreferredCourt);
    }

    @Test
    void whenUnspecDefendant1IndividualDefendant2Company_thenCourtIsClaimants() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(10000_00))
                            .build())
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(CourtLocation.builder()
                               .caseLocation(claimantPreferredCourt)
                               .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("123")
                                       .caseLocation(defendant1PreferredCourt)
                                       .build()
                               )
                               .build())
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("123")
                                       .caseLocation(defendant2PreferredCourt)
                                       .build()
                               )
                               .build())
            // individual answered first
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent2ResponseDate(LocalDateTime.now().minusDays(2))
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(claimantPreferredCourt);
    }

    @Test
    void whenSpecDefendantIsPersonAndDefendant2IsPerson_courtIsWhoRespondedFirst() {
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
                                      .caseLocation(claimantPreferredCourt)
                                      .build()
                              )
                              .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("123")
                                       .caseLocation(defendant1PreferredCourt)
                                       .build())
                               .build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQRequestedCourt(
                                   RequestedCourt.builder()
                                       .responseCourtCode("123")
                                       .caseLocation(defendant2PreferredCourt)
                                       .build())
                               .build())
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent2ResponseDate(LocalDateTime.now().minusDays(1))
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(defendant2PreferredCourt);
    }

    @ParameterizedTest
    @MethodSource("whenDefendant1IsLeadDefendant")
    void whenDefendant1IsLead(CaseData caseData) {
        boolean defendant1IsLead = helper.leadDefendantIs1(caseData);
        Assertions.assertThat(defendant1IsLead).isTrue();
    }

    static Stream<Arguments> whenDefendant1IsLeadDefendant() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent2ResponseDate(null)
                    .build()
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent1ResponseDate(LocalDateTime.now().minusDays(1))
                    .build()
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent2(Party.builder().type(Party.Type.COMPANY).build())
                    .respondent1ResponseDate(LocalDateTime.now().minusDays(1))
                    .build()
            )
        );
    }

    @ParameterizedTest
    @MethodSource("whenDefendant2IsLeadDefendant")
    void whenDefendant2IsLead(CaseData caseData) {
        boolean defendant1IsLead = helper.leadDefendantIs1(caseData);
        Assertions.assertThat(defendant1IsLead).isFalse();
    }

    static Stream<Arguments> whenDefendant2IsLeadDefendant() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent2ResponseDate(LocalDateTime.now().minusDays(1))
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent1ResponseDate(LocalDateTime.now())
                    .build()
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent1(Party.builder().type(Party.Type.COMPANY).build())
                    .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent1ResponseDate(LocalDateTime.now().minusDays(1))
                    .build()
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent2ResponseDate(LocalDateTime.now().minusDays(1))
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent1ResponseDate(null)
                    .build()
            )
        );
    }
}
