package uk.gov.hmcts.reform.civil.enums.caseprogression;

import java.util.Arrays;
import java.util.List;

public enum TypeOfDocDocumentaryEvidenceOfTrial {

    CHRONOLOGY("Chronology"),
    TIMETABLE("timetable", "time table", "time-table"),
    PART18("Part 18"),
    NOTICE_TO_ADMIT_FACTS("Notice to admit facts"),
    RESPONSE("Response");
    List<String> displayNames;

    public List<String> getDisplayNames() {
        return displayNames;
    }

    TypeOfDocDocumentaryEvidenceOfTrial(String... displayName) {
        this.displayNames = Arrays.asList(displayName);
    }
}
