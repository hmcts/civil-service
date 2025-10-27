package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.submitsdotests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.submitsdo.CourtLocationFieldUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
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
    void shouldSetEaCourtLocationToYesWhenSpecSolicitorCase() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("1010101").region("orange").build())
            .ccdCaseReference(1234L)
            .respondent1Represented(YES)
            .build();

        CaseData.CaseDataBuilder<?, ?> dataBuilder = caseData.toBuilder();
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        courtLocationFieldUpdater.update(caseData, dataBuilder);

        assertThat(dataBuilder.build().getEaCourtLocation()).isEqualTo(YES);
    }

    @Test
    void shouldSetEaCourtLocationToNoWhenSpecLipCaseNotWhitelisted() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("1010101").region("orange").build())
            .ccdCaseReference(1234L)
            .respondent1Represented(NO)
            .applicant1Represented(NO)
            .build();

        CaseData.CaseDataBuilder<?, ?> dataBuilder = caseData.toBuilder();
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        courtLocationFieldUpdater.update(caseData, dataBuilder);

        assertThat(dataBuilder.build().getEaCourtLocation()).isEqualTo(NO);
    }

    @Test
    void shouldNotSetEaCourtLocationForUnspecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("1010101").region("orange").build())
            .ccdCaseReference(1234L)
            .respondent1Represented(YES)
            .build();

        CaseData.CaseDataBuilder<?, ?> dataBuilder = caseData.toBuilder();
        courtLocationFieldUpdater.update(caseData, dataBuilder);

        assertThat(dataBuilder.build().getEaCourtLocation()).isNull();
    }
}
