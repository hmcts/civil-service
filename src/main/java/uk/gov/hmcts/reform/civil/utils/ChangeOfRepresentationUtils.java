package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

public class ChangeOfRepresentationUtils {

    private ChangeOfRepresentationUtils() {
        //NO-OP
    }

    public static ChangeOfRepresentation getLatestChangeOfRepresentation(
        List<Element<ChangeOfRepresentation>> changeOfRepresentationHistory) {
        if (changeOfRepresentationHistory.size() > 1) {
            List<ChangeOfRepresentation> changeOfRepresentations = unwrapElements(changeOfRepresentationHistory);
            ArrayList<ChangeOfRepresentation> changeOfRepresentations1 = new ArrayList<>(changeOfRepresentations);
            changeOfRepresentations1.sort(sortNoCEvents());
        }
        return changeOfRepresentationHistory.get(0).getValue();
    }

    private static Comparator<ChangeOfRepresentation> sortNoCEvents() {
        return (nocEvent1, nocEvent2) -> {
            if (nocEvent1.getTimestamp().isAfter(nocEvent2.getTimestamp())) {
                return -1;
            } else if (nocEvent1.getTimestamp().isBefore(nocEvent2.getTimestamp())) {
                return 1;
            } else {
                return 0;
            }
        };
    }
}
