package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypesLR;

import java.util.ArrayList;
import java.util.List;

public class GATypeHelper {

    private GATypeHelper() {
        // Utility class, no instances
    }

    public static List<GeneralApplicationTypes> getGATypes(List<GeneralApplicationTypesLR> listGATypeLr) {
        List<GeneralApplicationTypes> types = new ArrayList<GeneralApplicationTypes>();
        for (GeneralApplicationTypesLR gaTypeLR: listGATypeLr) {
            types.add(getGAType(gaTypeLR));
        }
        return  types;
    }

    public static GeneralApplicationTypes getGAType(GeneralApplicationTypesLR gaTypeLR) {

        switch (gaTypeLR) {
            case SETTLE_BY_CONSENT:
                return GeneralApplicationTypes.SETTLE_BY_CONSENT;
            case ADJOURN_HEARING:
                return GeneralApplicationTypes.ADJOURN_HEARING;
            case AMEND_A_STMT_OF_CASE:
                return GeneralApplicationTypes.AMEND_A_STMT_OF_CASE;
            case EXTEND_TIME:
                return GeneralApplicationTypes.EXTEND_TIME;
            case PROCEEDS_IN_HERITAGE:
                return GeneralApplicationTypes.PROCEEDS_IN_HERITAGE;
            case RELIEF_FROM_SANCTIONS:
                return GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
            case STAY_THE_CLAIM:
                return GeneralApplicationTypes.STAY_THE_CLAIM;
            case STRIKE_OUT:
                return GeneralApplicationTypes.STRIKE_OUT;
            case SUMMARY_JUDGEMENT:
                return GeneralApplicationTypes.SUMMARY_JUDGEMENT;
            case SET_ASIDE_JUDGEMENT:
                return GeneralApplicationTypes.SET_ASIDE_JUDGEMENT;
            case UNLESS_ORDER:
                return GeneralApplicationTypes.UNLESS_ORDER;
            case VARY_ORDER:
                return GeneralApplicationTypes.VARY_ORDER;
            case VARY_PAYMENT_TERMS_OF_JUDGMENT:
                return GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT;
            case OTHER:
                return GeneralApplicationTypes.OTHER;
            default:
                throw new IllegalArgumentException("Invalid General Application Type");
        }

    }
}
