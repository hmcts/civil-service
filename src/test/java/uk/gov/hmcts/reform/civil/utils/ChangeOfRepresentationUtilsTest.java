package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.utils.ChangeOfRepresentationUtils.getLatestChangeOfRepresentation;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class ChangeOfRepresentationUtilsTest {

    @Test
    void shouldReturnCorrectEvent_whenOneEventIsRecorded() {
        ChangeOfRepresentation changeOfRepresentation = ChangeOfRepresentation.builder()
            .organisationToRemoveID("remove")
            .organisationToAddID("add")
            .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
            .timestamp(LocalDateTime.now()).build();

        List<Element<ChangeOfRepresentation>> changeOfRepList = wrapElements(changeOfRepresentation);

        assertEquals(getLatestChangeOfRepresentation(changeOfRepList), changeOfRepresentation);
    }

    @Test
    void shouldReturnCorrectEvent_whenMultipleEventsAreRecorded() {
        ChangeOfRepresentation changeOfRepresentation1 = ChangeOfRepresentation.builder()
            .organisationToRemoveID("remove")
            .organisationToAddID("add")
            .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
            .timestamp(LocalDateTime.now()).build();

        List<Element<ChangeOfRepresentation>> changeOfRepListOld = wrapElements(changeOfRepresentation1);

        ChangeOfRepresentation changeOfRepresentation2 = ChangeOfRepresentation.builder()
            .organisationToRemoveID("remove")
            .organisationToAddID("add")
            .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
            .timestamp(LocalDateTime.now().minusDays(1)).build();

        ArrayList<ChangeOfRepresentation> changeOfRepresentationHistoryOld = new ArrayList<>();
        changeOfRepListOld.add(element(changeOfRepresentation2));

        ChangeOfRepresentation changeOfRepresentation3 = ChangeOfRepresentation.builder()
            .organisationToRemoveID("remove")
            .organisationToAddID("add")
            .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
            .timestamp(LocalDateTime.now().minusDays(3)).build();
        changeOfRepListOld.add(element(changeOfRepresentation3));

        assertEquals(getLatestChangeOfRepresentation(changeOfRepListOld), changeOfRepresentation1);
    }
}
