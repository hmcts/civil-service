package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.EmploymentTypeCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseData.CaseDataBuilder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EmploymentTypeCaseUpdaterTest {

    @InjectMocks
    private EmploymentTypeCaseUpdater employmentTypeCaseUpdater;

    @Test
    void shouldUpdateEmploymentTypeWhenRequired() {
        CaseData caseData = CaseData.builder()
                .defenceAdmitPartEmploymentTypeRequired(YesOrNo.YES)
                .respondToClaimAdmitPartEmploymentTypeLRspec(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED))
                .defenceAdmitPartEmploymentType2Required(YesOrNo.YES)
                .respondToClaimAdmitPartEmploymentTypeLRspec2(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED))
                .build();

        CaseDataBuilder<?, ?> updatedCaseDataBuilder = CaseData.builder();

        employmentTypeCaseUpdater.update(caseData, updatedCaseDataBuilder);
        CaseData updatedCaseData = updatedCaseDataBuilder.build();

        assertThat(updatedCaseData.getRespondToClaimAdmitPartEmploymentTypeLRspecGeneric()).isNotNull();
        assertThat(updatedCaseData.getRespondToClaimAdmitPartEmploymentTypeLRspecGeneric()).containsExactly(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED);
    }

    @Test
    void shouldNotUpdateEmploymentTypeWhenNotRequired() {
        CaseData caseData = CaseData.builder()
                .defenceAdmitPartEmploymentTypeRequired(YesOrNo.NO)
                .defenceAdmitPartEmploymentType2Required(YesOrNo.NO)
                .build();

        CaseDataBuilder<?, ?> updatedCaseDataBuilder = CaseData.builder();

        employmentTypeCaseUpdater.update(caseData, updatedCaseDataBuilder);
        CaseData updatedCaseData = updatedCaseDataBuilder.build();

        assertThat(updatedCaseData.getRespondToClaimAdmitPartEmploymentTypeLRspec()).isNull();
        assertThat(updatedCaseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2()).isNull();
    }
}