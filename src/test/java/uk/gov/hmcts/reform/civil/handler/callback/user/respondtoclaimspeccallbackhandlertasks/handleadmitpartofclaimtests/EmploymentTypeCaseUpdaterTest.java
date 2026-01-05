package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.EmploymentTypeCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EmploymentTypeCaseUpdaterTest {

    @InjectMocks
    private EmploymentTypeCaseUpdater employmentTypeCaseUpdater;

    @Test
    void shouldUpdateEmploymentTypeWhenRequired() {
        CaseData caseData = uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.builder().build();
        caseData.setDefenceAdmitPartEmploymentTypeRequired(YesOrNo.YES);
        caseData.setRespondToClaimAdmitPartEmploymentTypeLRspec(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED));
        caseData.setDefenceAdmitPartEmploymentType2Required(YesOrNo.YES);
        caseData.setRespondToClaimAdmitPartEmploymentTypeLRspec2(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED));

        employmentTypeCaseUpdater.update(caseData);

        assertThat(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspecGeneric()).isNotNull();
        assertThat(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspecGeneric()).containsExactly(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED);
    }

    @Test
    void shouldNotUpdateEmploymentTypeWhenNotRequired() {
        CaseData caseData = uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.builder().build();
        caseData.setDefenceAdmitPartEmploymentTypeRequired(YesOrNo.NO);
        caseData.setDefenceAdmitPartEmploymentType2Required(YesOrNo.NO);

        employmentTypeCaseUpdater.update(caseData);

        assertThat(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec()).isNull();
        assertThat(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2()).isNull();
    }
}
