package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
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
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .type(BreathingSpaceType.STANDARD)
                                      .build())
                           .lift(BreathingSpaceLiftInfo.builder()
                                     .expectedEnd(LocalDate.now())
                                     .build())
                           .build())
            .build();

        Mockito.when(featureToggleService.isSpecRpaContinuousFeedEnabled()).thenReturn(true);

        RoboticsCaseDataSpec mapped = mapper.toRoboticsCaseData(caseData);

        Assert.assertEquals(mapped.getHeader().getCaseNumber(), caseData.getLegacyCaseReference());
        Assert.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(caseData.getApplicant1().getPartyName())));
        Assert.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(caseData.getRespondent1().getPartyName())));
        Assert.assertEquals(caseData.getBreathing().getEnter().getType(), mapped.getBreathingSpace().getType());
        Assert.assertEquals(caseData.getBreathing().getLift().getExpectedEnd(),
                            mapped.getBreathingSpace().getEndDate());
    }
}
