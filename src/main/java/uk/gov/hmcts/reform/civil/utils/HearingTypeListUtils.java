package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.List;

public class HearingTypeListUtils {

    private HearingTypeListUtils() {
        //NO-OP
    }

    public static final DynamicList INTERMEDIATE_LIST;
    public static final DynamicList MULTI_LIST;

    static {
        DynamicListElement element1 = new DynamicListElement();
        element1.setCode("CASE_MANAGEMENT_CONFERENCE");
        element1.setLabel("Case Management Conference (CMC)");
        DynamicListElement element2 = new DynamicListElement();
        element2.setCode("PRE_TRIAL_REVIEW");
        element2.setLabel("Pre Trial Review (PTR)");
        DynamicListElement element3 = new DynamicListElement();
        element3.setCode("TRIAL");
        element3.setLabel("Trial");
        DynamicListElement element4 = new DynamicListElement();
        element4.setCode("OTHER");
        element4.setLabel("Other");
        DynamicList intermediateList = new DynamicList();
        intermediateList.setListItems(List.of(element1, element2, element3, element4));
        INTERMEDIATE_LIST = intermediateList;

        DynamicListElement element5 = new DynamicListElement();
        element5.setCode("CASE_MANAGEMENT_CONFERENCE");
        element5.setLabel("Case Management Conference (CMC)");
        DynamicListElement element6 = new DynamicListElement();
        element6.setCode("COSTS_CASE_MANAGEMENT_CONFERENCE");
        element6.setLabel("Costs and Case Management Conference (CCMC)");
        DynamicListElement element7 = new DynamicListElement();
        element7.setCode("PRE_TRIAL_REVIEW");
        element7.setLabel("Pre Trial Review (PTR)");
        DynamicListElement element8 = new DynamicListElement();
        element8.setCode("TRIAL");
        element8.setLabel("Trial");
        DynamicListElement element9 = new DynamicListElement();
        element9.setCode("OTHER");
        element9.setLabel("Other");
        DynamicList multiList = new DynamicList();
        multiList.setListItems(List.of(element5, element6, element7, element8, element9));
        MULTI_LIST = multiList;
    }
}

