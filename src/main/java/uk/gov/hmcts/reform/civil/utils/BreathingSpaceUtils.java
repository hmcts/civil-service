package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.StoredBreathingSpace;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public class BreathingSpaceUtils {

    public static final String ALREADY_IN_BREATHING_SPACE = "Case is already in Breathing Space";
    public static final String ALL_DEFENDANTS_LEFT =
        "All defendants on the case have already been in Breathing Space and left it";

    private static final String DEFENDANT_LABEL = "Breathing space for Defendant %d details";

    private BreathingSpaceUtils() {
    }

    public static Optional<String> getCannotEnterBreathingSpaceReason(CaseData caseData) {
        if (isBreathingSpaceActive(caseData)) {
            return Optional.of(ALREADY_IN_BREATHING_SPACE);
        }
        if (getCompletedBreathingSpaceCount(caseData) >= getDefendantCount(caseData)) {
            return Optional.of(ALL_DEFENDANTS_LEFT);
        }
        return Optional.empty();
    }

    public static int getDefendantCount(CaseData caseData) {
        return YES.equals(caseData.getAddRespondent2()) ? 2 : 1;
    }

    public static boolean isBreathingSpaceActive(CaseData caseData) {
        return caseData.getBreathing() != null
            && YES.equals(caseData.getBreathing().getActive());
    }

    public static int getCompletedBreathingSpaceCount(CaseData caseData) {
        return (int) getStoredBreathingSpaceItems(caseData.getBreathing()).stream()
            .filter(storedBreathingSpace -> storedBreathingSpace.getLift() != null)
            .count();
    }

    public static void addEnteredBreathingSpaceToHistory(CaseData caseData) {
        BreathingSpaceInfo breathingSpace = getOrCreateBreathingSpace(caseData);
        if (breathingSpace.getEnter() == null) {
            return;
        }
        addStoredBreathingSpace(
            breathingSpace,
            breathingSpace.getEnter(),
            breathingSpace.getStoredBreathingSpace().size() + 1
        );
        breathingSpace.setLift(null);
        sortStoredBreathingSpaceByStartDateDescending(breathingSpace);
    }

    public static void addLiftDetailsToCurrentBreathingSpace(CaseData caseData) {
        BreathingSpaceInfo breathingSpace = getOrCreateBreathingSpace(caseData);
        if (breathingSpace.getLift() == null) {
            return;
        }
        findUnliftedStoredBreathingSpace(breathingSpace)
            .ifPresent(item -> item.setLift(copyLiftBreathingSpaceInfo(breathingSpace.getLift())));
        sortStoredBreathingSpaceByStartDateDescending(breathingSpace);
    }

    private static BreathingSpaceInfo getOrCreateBreathingSpace(CaseData caseData) {
        if (caseData.getBreathing() == null) {
            caseData.setBreathing(new BreathingSpaceInfo());
        }
        List<Element<StoredBreathingSpace>> storedBreathingSpace = caseData.getBreathing().getStoredBreathingSpace();
        caseData.getBreathing().setStoredBreathingSpace(
            storedBreathingSpace == null ? new ArrayList<>() : new ArrayList<>(storedBreathingSpace)
        );
        return caseData.getBreathing();
    }

    private static List<StoredBreathingSpace> getStoredBreathingSpaceItems(BreathingSpaceInfo breathingSpace) {
        if (breathingSpace == null || breathingSpace.getStoredBreathingSpace() == null) {
            return List.of();
        }
        return breathingSpace.getStoredBreathingSpace().stream()
            .map(Element::getValue)
            .toList();
    }

    private static Optional<StoredBreathingSpace> findUnliftedStoredBreathingSpace(BreathingSpaceInfo breathingSpace) {
        return getStoredBreathingSpaceItems(breathingSpace).stream()
            .filter(item -> item.getLift() == null)
            .findFirst();
    }

    private static void addStoredBreathingSpace(BreathingSpaceInfo breathingSpace,
                                                BreathingSpaceEnterInfo enterInfo,
                                                int defendantNumber) {
        StoredBreathingSpace storedBreathingSpace = new StoredBreathingSpace()
            .setDefendantLabel(String.format(DEFENDANT_LABEL, defendantNumber))
            .setEnter(copyEnterBreathingSpaceInfo(enterInfo));
        breathingSpace.getStoredBreathingSpace().add(element(storedBreathingSpace));
    }

    private static void sortStoredBreathingSpaceByStartDateDescending(BreathingSpaceInfo breathingSpace) {
        breathingSpace.getStoredBreathingSpace().sort(
            Comparator.comparing(
                BreathingSpaceUtils::getStartDate,
                Comparator.nullsLast(Comparator.reverseOrder())
            )
        );
    }

    private static LocalDate getStartDate(Element<StoredBreathingSpace> storedBreathingSpace) {
        return storedBreathingSpace.getValue().getEnter().getStart();
    }

    private static BreathingSpaceEnterInfo copyEnterBreathingSpaceInfo(BreathingSpaceEnterInfo enterInfo) {
        return new BreathingSpaceEnterInfo()
            .setType(enterInfo.getType())
            .setReference(enterInfo.getReference())
            .setStart(enterInfo.getStart());
    }

    private static BreathingSpaceLiftInfo copyLiftBreathingSpaceInfo(BreathingSpaceLiftInfo liftInfo) {
        return new BreathingSpaceLiftInfo()
            .setExpectedEnd(liftInfo.getExpectedEnd())
            .setReasonToLift(liftInfo.getReasonToLift());
    }
}
