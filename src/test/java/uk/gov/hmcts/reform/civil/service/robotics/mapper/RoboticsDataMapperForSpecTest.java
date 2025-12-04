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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.NoticeOfChange;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@ExtendWith(SpringExtension.class)
class RoboticsDataMapperForSpecTest {

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
    private static final String BEARER_TOKEN = "Bearer Token";

    @Test
    void whenSpecEnabled_includeBS() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .totalInterest(BigDecimal.ZERO)
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .applicant1(createPartyWithCompany("company 1"))
            .respondent1(createPartyWithCompany("company 2"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build())
            .build();
        caseData.setSubmittedDate(LocalDateTime.now().minusDays(14));
        caseData.setBreathing(BreathingSpaceInfo.builder()
            .enter(BreathingSpaceEnterInfo.builder()
                .type(BreathingSpaceType.STANDARD)
                .build())
            .lift(BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now())
                .build())
            .build());

        RoboticsCaseDataSpec mapped = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        Assertions.assertEquals(mapped.getHeader().getCaseNumber(), caseData.getLegacyCaseReference());
        Assertions.assertTrue(mapped.getLitigiousParties().stream()
            .anyMatch(p -> p.getName().equals(caseData.getApplicant1().getPartyName())));
        Assertions.assertTrue(mapped.getLitigiousParties().stream()
            .anyMatch(p -> p.getName().equals(caseData.getRespondent1().getPartyName())));
    }

    @Test
    void shouldMapExpectedNoticeOfChangeData_whenCaseGoesOffline() {

        var app1NocDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");
        var res1NocDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
        var res2NocDate = LocalDateTime.parse("2022-03-01T12:00:00.000550439");

        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .totalInterest(BigDecimal.ZERO)
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .applicant1(createPartyWithCompany("company 1"))
            .respondent1(createPartyWithCompany("company 2"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build())
            .build();
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setBreathing(BreathingSpaceInfo.builder()
            .enter(BreathingSpaceEnterInfo.builder()
                .type(BreathingSpaceType.STANDARD)
                .build())
            .lift(BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now())
                .build())
            .build());
        caseData.setSubmittedDate(LocalDateTime.now().minusDays(14));

        OrganisationPolicy app1OrgPolicy = new OrganisationPolicy();
        app1OrgPolicy.setPreviousOrganisations(List.of(buildPreviousOrganisation("App 1 org", app1NocDate)));
        caseData.setApplicant1OrganisationPolicy(app1OrgPolicy);
        
        OrganisationPolicy res1OrgPolicy = new OrganisationPolicy();
        res1OrgPolicy.setPreviousOrganisations(List.of(buildPreviousOrganisation("Res 1 org", res1NocDate)));
        caseData.setRespondent1OrganisationPolicy(res1OrgPolicy);
        
        OrganisationPolicy res2OrgPolicy = new OrganisationPolicy();
        res2OrgPolicy.setPreviousOrganisations(List.of(buildPreviousOrganisation("Res 2 org", res2NocDate)));
        caseData.setRespondent2OrganisationPolicy(res2OrgPolicy);

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

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
    void shouldMapExpectedNoticeOfChangeData_whenCaseDismissed() {
        var app1NocDate = LocalDateTime.parse("2022-01-01T12:00:00.000550439");
        var res1NocDate = LocalDateTime.parse("2022-02-01T12:00:00.000550439");
        var res2NocDate = LocalDateTime.parse("2022-03-01T12:00:00.000550439");

        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .totalInterest(BigDecimal.ZERO)
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .applicant1(createPartyWithCompany("company 1"))
            .respondent1(createPartyWithCompany("company 2"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build())
            .build();
        caseData.setSubmittedDate(LocalDateTime.now().minusDays(14));
        caseData.setBreathing(BreathingSpaceInfo.builder()
            .enter(BreathingSpaceEnterInfo.builder()
                .type(BreathingSpaceType.STANDARD)
                .build())
            .lift(BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now())
                .build())
            .build());
        caseData.setCcdState(CaseState.CASE_DISMISSED);

        OrganisationPolicy app1OrgPolicy = new OrganisationPolicy();
        app1OrgPolicy.setPreviousOrganisations(List.of(buildPreviousOrganisation("App 1 org", app1NocDate)));
        caseData.setApplicant1OrganisationPolicy(app1OrgPolicy);
        
        OrganisationPolicy res1OrgPolicy = new OrganisationPolicy();
        res1OrgPolicy.setPreviousOrganisations(List.of(buildPreviousOrganisation("Res 1 org", res1NocDate)));
        caseData.setRespondent1OrganisationPolicy(res1OrgPolicy);
        
        OrganisationPolicy res2OrgPolicy = new OrganisationPolicy();
        res2OrgPolicy.setPreviousOrganisations(List.of(buildPreviousOrganisation("Res 2 org", res2NocDate)));
        caseData.setRespondent2OrganisationPolicy(res2OrgPolicy);

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

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
    void shouldNotPopulateNoticeOfChangeSection_whenCaseIsStillOnline() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .totalInterest(BigDecimal.ZERO)
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .applicant1(createPartyWithCompany("company 1"))
            .respondent1(createPartyWithCompany("company 2"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build())
            .build();
        caseData.setSubmittedDate(LocalDateTime.now().minusDays(14));
        caseData.setBreathing(BreathingSpaceInfo.builder()
            .enter(BreathingSpaceEnterInfo.builder()
                .type(BreathingSpaceType.STANDARD)
                .build())
            .lift(BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now())
                .build())
            .build());
        caseData.setCcdState(CaseState.CASE_ISSUED);

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        Assertions.assertNull(roboticsCaseData.getNoticeOfChange());
    }

    private PreviousOrganisationCollectionItem buildPreviousOrganisation(String name, LocalDateTime fromDate) {
        return PreviousOrganisationCollectionItem.builder().value(
            PreviousOrganisation.builder().organisationName(name).toTimestamp(fromDate).build()).build();
    }

    private Party createPartyWithCompany(String companyName) {
        Party party = new Party();
        party.setType(Party.Type.COMPANY);
        party.setCompanyName(companyName);
        return party;
    }

    @Test
    void shouldReturnNullCourtFee_whenClaimFeeIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .applicant1(createPartyWithCompany("Applicant"))
            .respondent1(createPartyWithCompany("Respondent"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@email.com").build())
            .build();
        caseData.setSubmittedDate(LocalDateTime.now());

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        Assertions.assertNull(roboticsCaseData.getClaimDetails().getCourtFee());
    }

    @Test
    void shouldReturnCalculatedCourtFee_whenNoHwfDetailsProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .claimFee(uk.gov.hmcts.reform.civil.model.Fee.builder()
                          .calculatedAmountInPence(BigDecimal.valueOf(10000)) // £100
                          .build())
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .applicant1(createPartyWithCompany("Applicant"))
            .respondent1(createPartyWithCompany("Respondent"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@email.com").build())
            .build();
        caseData.setSubmittedDate(LocalDateTime.now());

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        Assertions.assertEquals(0, roboticsCaseData.getClaimDetails().getCourtFee().compareTo(BigDecimal.valueOf(100)));
    }

    @Test
    void shouldReturnZeroCourtFee_whenHwfRemissionEqualsFee() {
        BigDecimal fullFee = BigDecimal.valueOf(20000); // £200

        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .claimFee(uk.gov.hmcts.reform.civil.model.Fee.builder()
                          .calculatedAmountInPence(fullFee)
                          .build())
            .claimIssuedHwfDetails(uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails.builder()
                                       .remissionAmount(fullFee)
                                       .build())
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .applicant1(createPartyWithCompany("Applicant"))
            .respondent1(createPartyWithCompany("Respondent"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@email.com").build())
            .build();
        caseData.setSubmittedDate(LocalDateTime.now());

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        Assertions.assertEquals(BigDecimal.ZERO, roboticsCaseData.getClaimDetails().getCourtFee());
    }

    @Test
    void shouldReturnOutstandingFee_whenHwfOutstandingFeeIsPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .claimFee(uk.gov.hmcts.reform.civil.model.Fee.builder()
                          .calculatedAmountInPence(BigDecimal.valueOf(30000)) // £300
                          .build())
            .claimIssuedHwfDetails(uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails.builder()
                                       .outstandingFeeInPounds(BigDecimal.valueOf(120))
                                       .build())
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .applicant1(createPartyWithCompany("Applicant"))
            .respondent1(createPartyWithCompany("Respondent"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@email.com").build())
            .build();
        caseData.setSubmittedDate(LocalDateTime.now());

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        Assertions.assertEquals(0, roboticsCaseData.getClaimDetails().getCourtFee().compareTo(BigDecimal.valueOf(120)));
    }

    @Test
    void shouldReturnCalculatedFee_whenRemissionNotEqualAndNoOutstandingFee() {
        BigDecimal fullFee = BigDecimal.valueOf(25000); // £250
        BigDecimal remission = BigDecimal.valueOf(5000); // partial remission

        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("reference")
            .claimFee(uk.gov.hmcts.reform.civil.model.Fee.builder()
                          .calculatedAmountInPence(fullFee)
                          .build())
            .claimIssuedHwfDetails(uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails.builder()
                                       .remissionAmount(remission)
                                       .build())
            .totalClaimAmount(BigDecimal.valueOf(5000))
            .applicant1(createPartyWithCompany("Applicant"))
            .respondent1(createPartyWithCompany("Respondent"))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@email.com").build())
            .build();
        caseData.setSubmittedDate(LocalDateTime.now());

        RoboticsCaseDataSpec roboticsCaseData = mapper.toRoboticsCaseData(caseData, BEARER_TOKEN);

        Assertions.assertEquals(0, roboticsCaseData.getClaimDetails().getCourtFee().compareTo(BigDecimal.valueOf(250)));
    }
}
