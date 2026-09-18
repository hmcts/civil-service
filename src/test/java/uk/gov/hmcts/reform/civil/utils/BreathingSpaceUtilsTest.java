package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.breathing.StoredBreathingSpace;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.BreathingSpaceUtils.ALL_DEFENDANTS_LEFT;
import static uk.gov.hmcts.reform.civil.utils.BreathingSpaceUtils.ALREADY_IN_BREATHING_SPACE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

class BreathingSpaceUtilsTest {

    @Test
    void defendantCountIsOneByDefaultAndTwoWhenSecondDefendantAdded() {
        CaseData oneDefendant = CaseData.builder().build();
        CaseData twoDefendants = CaseData.builder().addRespondent2(YesOrNo.YES).build();

        assertThat(BreathingSpaceUtils.getDefendantCount(oneDefendant)).isEqualTo(1);
        assertThat(BreathingSpaceUtils.getDefendantCount(twoDefendants)).isEqualTo(2);
    }

    @Test
    void cannotEnterWhenCaseIsAlreadyInBreathingSpace() {
        CaseData caseData = caseWith(enter(LocalDate.now()), null);
        caseData.getBreathing().setActive(YesOrNo.YES);

        assertThat(BreathingSpaceUtils.getCannotEnterBreathingSpaceReason(caseData)).contains(ALREADY_IN_BREATHING_SPACE);
    }

    @Test
    void cannotEnterWhenAllDefendantsHaveLeft() {
        CaseData caseData = caseWith(enter(LocalDate.now()), null);
        enterThenLift(caseData);

        assertThat(BreathingSpaceUtils.getCannotEnterBreathingSpaceReason(caseData)).contains(ALL_DEFENDANTS_LEFT);
    }

    @Test
    void allowsSecondEnterOnOneVTwoAfterFirstLift() {
        CaseData caseData = caseWith(enter(LocalDate.now()), null);
        caseData.setAddRespondent2(YesOrNo.YES);
        enterThenLift(caseData);

        assertThat(BreathingSpaceUtils.getCannotEnterBreathingSpaceReason(caseData)).isEmpty();
    }

    @Test
    void addEnteredBreathingSpaceToHistoryAppendsLabelledItemClearsLiftAndSortsByStartDescending() {
        BreathingSpaceEnterInfo firstEnter = enter(LocalDate.now().minusDays(10));
        BreathingSpaceEnterInfo secondEnter = enter(LocalDate.now());
        CaseData caseData = caseWith(firstEnter, null);
        caseData.setAddRespondent2(YesOrNo.YES);

        enterThenLift(caseData);
        caseData.getBreathing().setEnter(secondEnter);
        BreathingSpaceUtils.addEnteredBreathingSpaceToHistory(caseData);

        List<StoredBreathingSpace> stored = unwrapElements(caseData.getBreathing().getStoredBreathingSpace());
        assertThat(stored).hasSize(2);
        assertThat(stored.get(0).getDefendantLabel()).isEqualTo("Breathing space for Defendant 2 details");
        assertThat(stored.get(0).getEnter().getStart()).isEqualTo(secondEnter.getStart());
        assertThat(stored.get(0).getLift()).isNull();
        assertThat(stored.get(1).getDefendantLabel()).isEqualTo("Breathing space for Defendant 1 details");
        assertThat(stored.get(1).getLift()).isNotNull();
        assertThat(caseData.getBreathing().getLift()).isNull();
    }

    @Test
    void addLiftDetailsToCurrentBreathingSpaceWritesLiftOntoOpenStoredItem() {
        BreathingSpaceEnterInfo enterInfo = enter(LocalDate.now().minusDays(5));
        CaseData caseData = caseWith(enterInfo, null);

        enterThenLift(caseData);

        List<StoredBreathingSpace> stored = unwrapElements(caseData.getBreathing().getStoredBreathingSpace());
        assertThat(stored).hasSize(1);
        assertThat(stored.get(0).getDefendantLabel()).isEqualTo("Breathing space for Defendant 1 details");
        assertThat(stored.get(0).getEnter().getStart()).isEqualTo(enterInfo.getStart());
        assertThat(stored.get(0).getLift().getExpectedEnd()).isEqualTo(caseData.getBreathing().getLift().getExpectedEnd());
        assertThat(stored.get(0).getLift().getReasonToLift()).isEqualTo("reason");
    }

    @Test
    void addEnteredBreathingSpaceToHistoryCopiesEnterSoLaterFormChangesDoNotMutateHistory() {
        BreathingSpaceEnterInfo enterInfo = enter(LocalDate.now());
        CaseData caseData = caseWith(enterInfo, null);

        BreathingSpaceUtils.addEnteredBreathingSpaceToHistory(caseData);
        enterInfo.setReference("changed");

        assertThat(unwrapElements(caseData.getBreathing().getStoredBreathingSpace()).get(0)
                       .getEnter().getReference()).isEqualTo("ref");
    }

    private static void enterThenLift(CaseData caseData) {
        BreathingSpaceUtils.addEnteredBreathingSpaceToHistory(caseData);
        caseData.getBreathing().setLift(lift());
        BreathingSpaceUtils.addLiftDetailsToCurrentBreathingSpace(caseData);
    }

    private static CaseData caseWith(BreathingSpaceEnterInfo enter, BreathingSpaceLiftInfo lift) {
        BreathingSpaceInfo breathing = new BreathingSpaceInfo()
            .setEnter(enter)
            .setLift(lift);
        CaseData caseData = CaseData.builder().build();
        caseData.setBreathing(breathing);
        return caseData;
    }

    private static BreathingSpaceEnterInfo enter(LocalDate start) {
        return new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference("ref")
            .setStart(start);
    }

    private static BreathingSpaceLiftInfo lift() {
        return new BreathingSpaceLiftInfo()
            .setExpectedEnd(LocalDate.now().plusDays(10))
            .setReasonToLift("reason");
    }
}
