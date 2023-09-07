package uk.gov.hmcts.reform.civil.enums.caseprogression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TypeOfDocDocumentaryEvidenceOfTrial {

    CHRONOLOGY("Chronology"),
    TIMETABLE("timetable", "time table", "time-table"),
    PART18("Part 18", "Part18"),
    NOTICE_TO_ADMIT_FACTS("Notice to admit facts"),
    RESPONSE("Response"),
    SCHEDULE_OF_LOSS("Schedule of loss");
    List<String> displayNames;

    public List<String> getDisplayNames() {
        return displayNames;
    }

    public static List<String> getAllDocsDisplayNames() {
        List<TypeOfDocDocumentaryEvidenceOfTrial> list =
            new ArrayList<TypeOfDocDocumentaryEvidenceOfTrial>(Arrays.asList(values()));
        List<List<String>> listOfDocTypes =
            list.stream().map(typeOfDocDocumentaryEvidenceOfTrial -> typeOfDocDocumentaryEvidenceOfTrial.getDisplayNames()).collect(
            Collectors.toList());
        return listOfDocTypes.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    TypeOfDocDocumentaryEvidenceOfTrial(String... displayName) {
        this.displayNames = Arrays.asList(displayName);
    }
}
