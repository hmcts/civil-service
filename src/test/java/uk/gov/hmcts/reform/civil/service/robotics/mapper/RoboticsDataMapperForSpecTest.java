package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CustomScopeIdamTokenGeneratorService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistorySequencer.class,
    EventHistoryMapper.class,
    RoboticsDataMapperForSpec.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class,
    OrganisationService.class
})
public class RoboticsDataMapperForSpecTest {

    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    UserService userService;
    @MockBean
    PrdAdminUserConfiguration userConfig;
    @MockBean
    CustomScopeIdamTokenGeneratorService tokenGenerator;
    @MockBean
    private Time time;

    @Autowired
    RoboticsDataMapperForSpec mapperSpec;

    LocalDateTime localDateTime;

    @BeforeEach
    void setUp() {
        localDateTime = LocalDateTime.of(2020, 8, 1, 12, 0, 0);
        when(time.now()).thenReturn(localDateTime);
    }

    @Test
    public void whenSpecEnabled_includeBSEntered() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
            .breathing(BreathingSpaceInfo.builder()
                           .enter(BreathingSpaceEnterInfo.builder()
                                      .type(BreathingSpaceType.STANDARD)
                                      .build())
                           .build())
            .build();

        Mockito.when(featureToggleService.isSpecRpaContinuousFeedEnabled()).thenReturn(true);

        RoboticsCaseDataSpec mapped = mapperSpec.toRoboticsCaseData(caseData);

        Assert.assertEquals(mapped.getHeader().getCaseNumber(), caseData.getLegacyCaseReference());
        Assert.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(caseData.getApplicant1().getPartyName())));
        Assert.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(PartyUtils.getLitigiousPartyName(
                                  caseData.getApplicant1(), caseData.getApplicant1LitigationFriend()))));
        Assert.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(PartyUtils.getLitigiousPartyName(
                                  caseData.getRespondent1(), caseData.getRespondent1LitigationFriend()))));
        Assert.assertEquals(caseData.getBreathing().getEnter().getType(), mapped.getBreathingSpace().getType());
        assertNotNull(mapped.getEvents().getBreathingSpaceEntered());
        assertNotNull(mapped.getEvents().getBreathingSpaceLifted());
    }

    @Test
    public void whenSpecEnabled_includeBSLifted() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(15000_00))
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

        RoboticsCaseDataSpec mapped = mapperSpec.toRoboticsCaseData(caseData);

        Assert.assertEquals(mapped.getHeader().getCaseNumber(), caseData.getLegacyCaseReference());
        Assert.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(caseData.getApplicant1().getPartyName())));
        Assert.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(PartyUtils.getLitigiousPartyName(
                                  caseData.getApplicant1(), caseData.getApplicant1LitigationFriend()))));
        Assert.assertTrue(mapped.getLitigiousParties().stream()
                              .anyMatch(p -> p.getName().equals(PartyUtils.getLitigiousPartyName(
                                  caseData.getRespondent1(), caseData.getRespondent1LitigationFriend()))));
        Assert.assertEquals(caseData.getBreathing().getEnter().getType(), mapped.getBreathingSpace().getType());
        Assert.assertEquals(caseData.getBreathing().getLift().getExpectedEnd(),
                            mapped.getBreathingSpace().getEndDate());
        assertNotNull(mapped.getEvents().getBreathingSpaceLifted());
    }
}
