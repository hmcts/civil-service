package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypesLR;

import static org.assertj.core.api.Assertions.assertThat;

public class GATypeHelperTest {

    @Test
    void test_get_gaType_based_on_Ga_tye_for_LR() {
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.ADJOURN_HEARING)).isEqualTo(GeneralApplicationTypes.ADJOURN_HEARING);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.AMEND_A_STMT_OF_CASE)).isEqualTo(GeneralApplicationTypes.AMEND_A_STMT_OF_CASE);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.SETTLE_BY_CONSENT)).isEqualTo(GeneralApplicationTypes.SETTLE_BY_CONSENT);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.PROCEEDS_IN_HERITAGE)).isEqualTo(GeneralApplicationTypes.PROCEEDS_IN_HERITAGE);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.SET_ASIDE_JUDGEMENT)).isEqualTo(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.EXTEND_TIME)).isEqualTo(GeneralApplicationTypes.EXTEND_TIME);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.RELIEF_FROM_SANCTIONS)).isEqualTo(GeneralApplicationTypes.RELIEF_FROM_SANCTIONS);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.VARY_PAYMENT_TERMS_OF_JUDGMENT)).isEqualTo(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.VARY_ORDER)).isEqualTo(GeneralApplicationTypes.VARY_ORDER);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.SUMMARY_JUDGEMENT)).isEqualTo(GeneralApplicationTypes.SUMMARY_JUDGEMENT);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.UNLESS_ORDER)).isEqualTo(GeneralApplicationTypes.UNLESS_ORDER);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.STAY_THE_CLAIM)).isEqualTo(GeneralApplicationTypes.STAY_THE_CLAIM);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.STRIKE_OUT)).isEqualTo(GeneralApplicationTypes.STRIKE_OUT);
        assertThat(GATypeHelper.getGAType(GeneralApplicationTypesLR.OTHER)).isEqualTo(GeneralApplicationTypes.OTHER);
    }
}
