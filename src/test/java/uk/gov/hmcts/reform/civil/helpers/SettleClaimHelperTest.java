package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SettleClaimHelperTest {

    @Test
    void test_state() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        List<String> errors = new ArrayList<>();
        SettleClaimHelper.checkState(caseData, errors);
        assertThat(errors).isEmpty();
    }

    @Test
    void test_state_all_final_orders_issued() {
        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        List<String> errors = new ArrayList<>();
        SettleClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotEmpty();
    }
}
