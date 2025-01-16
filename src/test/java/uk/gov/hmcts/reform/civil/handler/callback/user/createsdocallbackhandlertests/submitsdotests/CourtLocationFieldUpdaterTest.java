package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.submitsdotests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.CourtLocationFieldUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class CourtLocationFieldUpdaterTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CourtLocationFieldUpdater courtLocationFieldUpdater;

    @Test
    void shouldUpdateEaCourtLocationToYesWhenCaseIsWhitelisted() {
        when(featureToggleService.isLocationWhiteListedForCaseProgression("1010101")).thenReturn(true);

        CaseData caseData = CaseData.builder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("1010101").region("orange").build())
                .ccdCaseReference(1234L)
                .build();
        CaseData.CaseDataBuilder<?, ?> dataBuilder = CaseData.builder();

        courtLocationFieldUpdater.update(caseData, dataBuilder);

        assertEquals(YES, dataBuilder.build().getEaCourtLocation());
    }

    @Test
    void shouldUpdateEaCourtLocationToNoWhenCaseIsNotWhitelisted() {
        CaseData caseData = CaseData.builder()
                .ccdCaseReference(1234L)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("1010101").region("orange").build())
                .respondent1Represented(NO)
                .build();
        CaseData.CaseDataBuilder<?, ?> dataBuilder = CaseData.builder();

        courtLocationFieldUpdater.update(caseData, dataBuilder);

        assertEquals(NO, dataBuilder.build().getEaCourtLocation());
    }
}