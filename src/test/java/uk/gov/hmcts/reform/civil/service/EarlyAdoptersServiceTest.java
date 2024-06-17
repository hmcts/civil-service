package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EarlyAdoptersServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private EarlyAdoptersService earlyAdoptersService;

    @ParameterizedTest
    @CsvSource({
        "true, true, true",
        "true, false, false",
        "false, true, false"})
    void isPartOfHmcEarlyAdoptersRollout(boolean isCmlWhiteListed, boolean isHearingLocationWhiteListed, boolean expected) {
        String caseManagementLocation = "111111";
        String hearingLocation = "222222";

        when(featureToggleService.isLocationWhiteListedForCaseProgression(eq(caseManagementLocation)))
            .thenReturn(isCmlWhiteListed);

        if (isCmlWhiteListed) {
            when(featureToggleService.isLocationWhiteListedForCaseProgression(eq(hearingLocation)))
                .thenReturn(isHearingLocationWhiteListed);
        }

        boolean actual = earlyAdoptersService.isPartOfHmcEarlyAdoptersRollout(
            caseDataWithCML(caseManagementLocation), hearingLocation);

        assertEquals(expected, actual);
    }

    private CaseData caseDataWithCML(String cmlEpimms) {
        return CaseData.builder().caseManagementLocation(
            CaseLocationCivil.builder()
                .baseLocation(cmlEpimms)
                .build()
        ).build();
    }
}
