package uk.gov.hmcts.reform.civil.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDataDateNormaliserTest {

    private final CaseDataDateNormaliser normaliser =
        new CaseDataDateNormaliser(new ObjectMapper().registerModule(new JavaTimeModule()), true);

    @Test
    void writesADateTimeSentForADateFieldAsADate() {
        // As civil-citizen-ui sends a full admission's payment date.
        Map<String, Object> updates = Map.of(
            "respondToClaimAdmitPartLRspec", Map.of("whenWillThisAmountBePaid", "2026-11-24T00:00:00.000Z")
        );

        assertThat(normaliser.normalise(updates, CaseData.class))
            .isEqualTo(Map.of("respondToClaimAdmitPartLRspec", Map.of("whenWillThisAmountBePaid", "2026-11-24")));
    }

    @Test
    void writesTheDatesInACollectionsItems() {
        Map<String, Object> updates = Map.of("applicant1", Map.of(
            "unavailableDates", List.of(Map.of("id", "1", "value", Map.of("date", "2026-12-01T00:00:00.000Z")))
        ));

        assertThat(normaliser.normalise(updates, CaseData.class)).isEqualTo(Map.of("applicant1", Map.of(
            "unavailableDates", List.of(Map.of("id", "1", "value", Map.of("date", "2026-12-01")))
        )));
    }

    @Test
    void leavesDatesDateTimesAndFieldsTheModelDoesNotKnowAsTheyWereSent() {
        Map<String, Object> updates = Map.of(
            "respondToClaimAdmitPartLRspec", Map.of("whenWillThisAmountBePaid", "2026-11-24"),
            "submittedDate", "2026-11-17T10:15:30",
            "notACaseField", "2026-11-24T00:00:00.000Z"
        );

        assertThat(normaliser.normalise(updates, CaseData.class)).isEqualTo(updates);
    }

    @Test
    void sendsTheUpdatesAsTheyCameWhenCasesAreNotDecentralised() {
        CaseDataDateNormaliser centralised =
            new CaseDataDateNormaliser(new ObjectMapper().registerModule(new JavaTimeModule()), false);
        Map<String, Object> updates = Map.of(
            "respondToClaimAdmitPartLRspec", Map.of("whenWillThisAmountBePaid", "2026-11-24T00:00:00.000Z")
        );

        assertThat(centralised.normalise(updates, CaseData.class)).isSameAs(updates);
    }

    @Test
    void leavesAValueTheModelCannotReadAsADateForCcdToReport() {
        Map<String, Object> updates = Map.of(
            "respondToClaimAdmitPartLRspec", Map.of("whenWillThisAmountBePaid", "not a date")
        );

        assertThat(normaliser.normalise(updates, CaseData.class)).isEqualTo(updates);
    }
}
