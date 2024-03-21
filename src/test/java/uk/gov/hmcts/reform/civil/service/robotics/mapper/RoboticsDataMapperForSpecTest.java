package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisation;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisationCollectionItem;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.NoticeOfChange;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@ExtendWith(SpringExtension.class)
public class RoboticsDataMapperForSpecTest {

    @InjectMocks
    private RoboticsDataMapperForSpec mapper;

    @Mock
    private RoboticsAddressMapper addressMapper;
    @Mock
    private EventHistoryMapper eventHistoryMapper;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    public void whenSpecEnabled_includeBS() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .submittedDate(LocalDateTime.now().minusDays(14))
            .totalInterest(BigDecimal.ZERO)
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("company 1")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company 2")
                             .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build())
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .type(BreathingSpaceType.STANDARD)
                                      .build())
                           .lift(BreathingSpaceLiftInfo.builder()
                                     .expectedEnd(LocalDate.now())
                                     .build())
                           .build())
            .build();

        RoboticsCaseDataSpec mapped = mapper.toRoboticsCaseData(caseData);

        Assertions.assertEquals(mapped.getHeader().getCaseNumber(), caseData.getLegacyCaseReference());
        Assertions.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(caseData.getApplicant1().getPartyName())));
        Assertions.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(caseData.getRespondent1().getPartyName())));
    }

    @Test
    public void shouldMapExpectedNoticeOfChangeData_whenCaseGoesOffline() {

        var app1NocDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");
        var res1NocDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
        var res2NocDate = LocalDateTime.parse("2022-03-01T12:00:00.000550439");

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .submittedDate(LocalDateTime.now().minusDays(14))
            .totalInterest(BigDecimal.ZERO)
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("company 1")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company 2")
                             .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build())
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .type(BreathingSpaceType.STANDARD)
                                      .build())
                           .lift(BreathingSpaceLiftInfo.builder()
                                     .expectedEnd(LocalDate.now())
                                     .build())
                           .build())
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .build();

        caseData = caseData.toBuilder()
            .applicant1OrganisationPolicy(
                OrganisationPolicy.builder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("App 1 org", app1NocDate)))
                    .build())
            .respondent1OrganisationPolicy(
                OrganisationPolicy.builder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("Res 1 org", res1NocDate)))
                    .build())
            .respondent2OrganisationPolicy(
                OrganisationPolicy.builder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("Res 2 org", res2NocDate)))
                    .build())
            .build();

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        Assertions.assertEquals(
            List.of(
                NoticeOfChange.builder().litigiousPartyID("001").dateOfNoC(app1NocDate.format(ISO_DATE)).build(),
                NoticeOfChange.builder().litigiousPartyID("002").dateOfNoC(res1NocDate.format(ISO_DATE)).build(),
                NoticeOfChange.builder().litigiousPartyID("003").dateOfNoC(res2NocDate.format(ISO_DATE)).build()
            ),
            roboticsCaseData.getNoticeOfChange()
        );
    }

    @Test
    public void shouldMapExpectedNoticeOfChangeData_whenCaseDismissed() {
        var app1NocDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");
        var res1NocDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
        var res2NocDate = LocalDateTime.parse("2022-03-01T12:00:00.000550439");

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .submittedDate(LocalDateTime.now().minusDays(14))
            .totalInterest(BigDecimal.ZERO)
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("company 1")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company 2")
                             .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build())
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .type(BreathingSpaceType.STANDARD)
                                      .build())
                           .lift(BreathingSpaceLiftInfo.builder()
                                     .expectedEnd(LocalDate.now())
                                     .build())
                           .build())
            .ccdState(CaseState.CASE_DISMISSED)
            .build();

        caseData = caseData.toBuilder()
            .applicant1OrganisationPolicy(
                OrganisationPolicy.builder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("App 1 org", app1NocDate)))
                    .build())
            .respondent1OrganisationPolicy(
                OrganisationPolicy.builder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("Res 1 org", res1NocDate)))
                    .build())
            .respondent2OrganisationPolicy(
                OrganisationPolicy.builder()
                    .previousOrganisations(List.of(buildPreviousOrganisation("Res 2 org", res2NocDate)))
                    .build())
            .build();

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        Assertions.assertEquals(
            List.of(
                NoticeOfChange.builder().litigiousPartyID("001").dateOfNoC(app1NocDate.format(ISO_DATE)).build(),
                NoticeOfChange.builder().litigiousPartyID("002").dateOfNoC(res1NocDate.format(ISO_DATE)).build(),
                NoticeOfChange.builder().litigiousPartyID("003").dateOfNoC(res2NocDate.format(ISO_DATE)).build()
            ),
            roboticsCaseData.getNoticeOfChange()
        );
    }

    @Test
    public void shouldNotPopulateNoticeOfChangeSection_whenCaseIsStillOnline() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .submittedDate(LocalDateTime.now().minusDays(14))
            .totalInterest(BigDecimal.ZERO)
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("company 1")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company 2")
                             .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build())
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .type(BreathingSpaceType.STANDARD)
                                      .build())
                           .lift(BreathingSpaceLiftInfo.builder()
                                     .expectedEnd(LocalDate.now())
                                     .build())
                           .build())
            .ccdState(CaseState.CASE_ISSUED)
            .build();

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        Assertions.assertNull(roboticsCaseData.getNoticeOfChange());
    }

    private PreviousOrganisationCollectionItem buildPreviousOrganisation(String name, LocalDateTime fromDate) {
        return PreviousOrganisationCollectionItem.builder().value(
            PreviousOrganisation.builder().organisationName(name).toTimestamp(fromDate).build()).build();
    }
}
