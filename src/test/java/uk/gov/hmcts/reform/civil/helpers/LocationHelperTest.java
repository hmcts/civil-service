package uk.gov.hmcts.reform.civil.helpers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@ExtendWith(MockitoExtension.class)
class LocationHelperTest {

    private LocationHelper helper;
    private final CaseLocationCivil claimantPreferredCourt = new CaseLocationCivil()
        .setBaseLocation("123456").setRegion("region 1");
    private final CaseLocationCivil defendant1PreferredCourt = new CaseLocationCivil()
        .setBaseLocation("987456").setRegion("region 1");
    private final CaseLocationCivil defendant2PreferredCourt = new CaseLocationCivil()
        .setBaseLocation("101010").setRegion("region 3");

    @BeforeEach
    void setup() {
        helper = new LocationHelper();
    }

    @Test
    void thereIsAMatchingLocation() {
        CaseData updatedData = CaseData.builder().build();
        List<LocationRefData> locations = List.of(LocationRefData.builder()
                                                      .courtLocationCode("123")
                                                      .regionId("regionId")
                                                      .region("region name")
                                                      .epimmsId("99999")
                                                      .build());
        RequestedCourt requestedCourt = new RequestedCourt()
            .setCaseLocation(new CaseLocationCivil().setBaseLocation("99999"));
        helper.updateCaseManagementLocation(updatedData, requestedCourt, () -> locations);
        Assertions.assertThat(updatedData.getCaseManagementLocation())
            .isNotNull()
            .isEqualTo(new CaseLocationCivil()
                           .setRegion("regionId")
                           .setBaseLocation("99999")
            );
    }

    @Test
    void whenSpecDefendantIsPerson_courtIsDefendantsPreferred() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQRequestedCourt(
                                  new RequestedCourt()
                                      .setCaseLocation(claimantPreferredCourt)
                              ))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(
                                   new RequestedCourt()
                                       .setCaseLocation(defendant1PreferredCourt)
                                       .setResponseCourtCode("123")
                               ))
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(defendant1PreferredCourt);
    }

    @Test
    void whenLessThan1000AndSpecifiedClaim_locationIsCcmcc() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .claimValue(new ClaimValue()
                            .setStatementOfValueInPennies(BigDecimal.valueOf(1000_00)))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQRequestedCourt(
                                  new RequestedCourt()
                                      .setCaseLocation(claimantPreferredCourt)
                              ))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(
                                   new RequestedCourt()
                                       .setCaseLocation(defendant1PreferredCourt)
                                       .setResponseCourtCode("123")
                               ))
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation())
            .isEqualTo(new CaseLocationCivil().setBaseLocation("987456").setRegion("region 1"));
    }

    @Test
    void whenLessThan1000AndDuringSdo_locationIsPreferredLocation() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(999))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQRequestedCourt(
                                  new RequestedCourt()
                                      .setCaseLocation(claimantPreferredCourt)
                              ))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(
                                   new RequestedCourt()
                                       .setCaseLocation(defendant1PreferredCourt)
                                       .setResponseCourtCode("123")
                               ))
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(defendant1PreferredCourt);
    }

    @Test
    void whenDefendantIsPerson_courtIsClaimantsPreferredAsUnspec() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(new ClaimValue()
                            .setStatementOfValueInPennies(BigDecimal.valueOf(10000_00)))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(new CourtLocation()
                               .setCaseLocation(claimantPreferredCourt))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(
                                   new RequestedCourt()
                                       .setCaseLocation(defendant1PreferredCourt)
                                       .setResponseCourtCode("123")
                               ))
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
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQRequestedCourt(
                                  new RequestedCourt()
                                      .setCaseLocation(claimantPreferredCourt)
                              ))
            .respondent1(Party.builder()
                             .type(Party.Type.ORGANISATION)
                             .build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(
                                   new RequestedCourt()
                                       .setCaseLocation(defendant1PreferredCourt)
                                       .setResponseCourtCode("123")
                               ))
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(claimantPreferredCourt);
    }

    @Test
    void whenUnspecDefendantIsGroup_courtIsClaimantsPreferred() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(new ClaimValue()
                            .setStatementOfValueInPennies(BigDecimal.valueOf(10000_00)))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(new CourtLocation()
                               .setCaseLocation(claimantPreferredCourt))
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(
                                   new RequestedCourt()
                                       .setCaseLocation(defendant1PreferredCourt)
                                       .setResponseCourtCode("123")
                               ))
            .build();

        Optional<RequestedCourt> court = helper.getCaseManagementLocation(caseData);
        Assertions.assertThat(court.orElseThrow().getCaseLocation()).isEqualTo(claimantPreferredCourt);
    }

    @Test
    void whenUnspecDefendant1IndividualDefendant2Company_thenCourtIsClaimants() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .claimValue(new ClaimValue()
                            .setStatementOfValueInPennies(BigDecimal.valueOf(10000_00)))
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .courtLocation(new CourtLocation()
                               .setCaseLocation(claimantPreferredCourt))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(
                                   new RequestedCourt()
                                       .setResponseCourtCode("123")
                                       .setCaseLocation(defendant1PreferredCourt)
                               ))
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .build())
            .respondent2DQ(new Respondent2DQ()
                               .setRespondent2DQRequestedCourt(
                                   new RequestedCourt()
                                       .setResponseCourtCode("123")
                                       .setCaseLocation(defendant2PreferredCourt)
                               ))
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
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQRequestedCourt(
                                  new RequestedCourt()
                                      .setResponseCourtCode("123")
                                      .setCaseLocation(claimantPreferredCourt)
                              ))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(
                                   new RequestedCourt()
                                       .setResponseCourtCode("123")
                                       .setCaseLocation(defendant1PreferredCourt)
                               ))
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .respondent2DQ(new Respondent2DQ()
                               .setRespondent2DQRequestedCourt(
                                   new RequestedCourt()
                                       .setResponseCourtCode("123")
                                       .setCaseLocation(defendant2PreferredCourt)
                               ))
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
