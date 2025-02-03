package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.List;

public class DynamicListUtils {

    private DynamicListUtils() {
        //NO-OP
    }

    public static List<String> listFromDynamicList(DynamicList dynamicList) {
        if (dynamicList != null
            && dynamicList.getListItems() != null) {
            return dynamicList.getListItems()
                .stream()
                .map(DynamicListElement::getLabel)
                .toList();
        }
        return null;
    }

    public static String getDynamicListValue(DynamicList dynamicList) {
        if (dynamicList != null
            && dynamicList.getValue() != null) {
            return dynamicList.getValue().getLabel();
        }
        return null;
    }
}
