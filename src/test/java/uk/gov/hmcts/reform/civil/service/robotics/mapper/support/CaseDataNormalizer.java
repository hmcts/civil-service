package uk.gov.hmcts.reform.civil.service.robotics.mapper.support;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Normalises dynamic timestamps produced by {@link uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder}
 * so golden EventHistory snapshots remain deterministic across runs.
 */
public final class CaseDataNormalizer {

    private CaseDataNormalizer() {
        // utility
    }

    public static CaseData normalise(CaseData caseData, LocalDate baseDate) {
        if (caseData == null) {
            return null;
        }

        LocalDateTime base = baseDate.atTime(12, 0);
        LocalDateTime day1 = base.plusDays(1);
        LocalDateTime day2 = base.plusDays(2);
        LocalDateTime day3 = base.plusDays(3);
        LocalDateTime day4 = base.plusDays(4);
        LocalDateTime day5 = base.plusDays(5);
        LocalDateTime day6 = base.plusDays(6);

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        if (caseData.getSubmittedDate() != null) {
            builder.submittedDate(base);
        }
        if (caseData.getPaymentSuccessfulDate() != null) {
            builder.paymentSuccessfulDate(base.plusHours(1));
        }
        if (caseData.getIssueDate() != null) {
            builder.issueDate(baseDate);
        }
        if (caseData.getClaimNotificationDate() != null) {
            builder.claimNotificationDate(day1);
        }
        if (caseData.getClaimNotificationDeadline() != null) {
            builder.claimNotificationDeadline(day1.plusHours(6));
        }
        if (caseData.getClaimDetailsNotificationDate() != null) {
            builder.claimDetailsNotificationDate(day2);
        }
        if (caseData.getClaimDetailsNotificationDeadline() != null) {
            builder.claimDetailsNotificationDeadline(day2.plusHours(6));
        }
        if (caseData.getRespondent1AcknowledgeNotificationDate() != null) {
            builder.respondent1AcknowledgeNotificationDate(day2.plusHours(2));
        }
        if (caseData.getRespondent2AcknowledgeNotificationDate() != null) {
            builder.respondent2AcknowledgeNotificationDate(day2.plusHours(4));
        }
        if (caseData.getRespondent1ResponseDeadline() != null) {
            builder.respondent1ResponseDeadline(day3);
        }
        if (caseData.getRespondent2ResponseDeadline() != null) {
            builder.respondent2ResponseDeadline(day3.plusHours(2));
        }
        if (caseData.getRespondent1ResponseDate() != null) {
            builder.respondent1ResponseDate(day3.plusHours(1));
        }
        if (caseData.getRespondent2ResponseDate() != null) {
            builder.respondent2ResponseDate(day3.plusHours(3));
        }
        if (caseData.getApplicant1ResponseDeadline() != null) {
            builder.applicant1ResponseDeadline(day4);
        }
        if (caseData.getApplicant1ResponseDate() != null) {
            builder.applicant1ResponseDate(day4.plusHours(1));
        }
        if (caseData.getApplicant2ResponseDate() != null) {
            builder.applicant2ResponseDate(day4.plusHours(3));
        }
        if (caseData.getTakenOfflineDate() != null) {
            builder.takenOfflineDate(day5);
        }
        if (caseData.getTakenOfflineByStaffDate() != null) {
            builder.takenOfflineByStaffDate(day5.plusHours(1));
        }
        if (caseData.getClaimDismissedDate() != null) {
            builder.claimDismissedDate(day6);
        }
        if (caseData.getCaseDismissedHearingFeeDueDate() != null) {
            builder.caseDismissedHearingFeeDueDate(day6.plusHours(1));
        }
        if (caseData.getAddLegalRepDeadlineRes1() != null) {
            builder.addLegalRepDeadlineRes1(day2.plusHours(8));
        }
        if (caseData.getAddLegalRepDeadlineRes2() != null) {
            builder.addLegalRepDeadlineRes2(day2.plusHours(10));
        }
        if (caseData.getRespondent1TimeExtensionDate() != null) {
            builder.respondent1TimeExtensionDate(day2.plusHours(12));
        }
        if (caseData.getRespondent2TimeExtensionDate() != null) {
            builder.respondent2TimeExtensionDate(day2.plusHours(14));
        }

        builder.claimProceedsInCaseman(normaliseClaimProceeds(caseData.getClaimProceedsInCaseman(), baseDate.plusDays(5)));
        builder.claimProceedsInCasemanLR(normaliseClaimProceeds(caseData.getClaimProceedsInCasemanLR(), baseDate.plusDays(5)));
        builder.breathing(normaliseBreathing(caseData.getBreathing(), baseDate));
        builder.generalApplications(normaliseGeneralApplications(caseData.getGeneralApplications(), day5));
        builder.gaDetailsMasterCollection(normaliseGeneralApplicationsDetails(caseData.getGaDetailsMasterCollection(), day5));
        builder.gaDetailsTranslationCollection(normaliseGeneralApplicationsDetails(caseData.getGaDetailsTranslationCollection(), day5));
        builder.caseNotes(normaliseCaseNotes(caseData.getCaseNotes(), day5));

        return builder.build();
    }

    private static ClaimProceedsInCaseman normaliseClaimProceeds(ClaimProceedsInCaseman value, LocalDate targetDate) {
        if (value == null) {
            return null;
        }
        return ClaimProceedsInCaseman.builder()
            .date(targetDate)
            .reason(value.getReason())
            .other(value.getOther())
            .build();
    }

    private static ClaimProceedsInCasemanLR normaliseClaimProceeds(ClaimProceedsInCasemanLR value, LocalDate targetDate) {
        if (value == null) {
            return null;
        }
        return ClaimProceedsInCasemanLR.builder()
            .date(targetDate)
            .reason(value.getReason())
            .other(value.getOther())
            .build();
    }

    private static BreathingSpaceInfo normaliseBreathing(BreathingSpaceInfo breathing, LocalDate baseDate) {
        if (breathing == null) {
            return null;
        }

        BreathingSpaceEnterInfo enter = breathing.getEnter();
        BreathingSpaceLiftInfo lift = breathing.getLift();

        BreathingSpaceEnterInfo updatedEnter = enter == null ? null : BreathingSpaceEnterInfo.builder()
            .type(enter.getType())
            .reference(enter.getReference())
            .start(baseDate.plusDays(8))
            .expectedEnd(enter.getExpectedEnd() == null ? null : baseDate.plusDays(9))
            .event(enter.getEvent())
            .eventDescription(enter.getEventDescription())
            .build();

        BreathingSpaceLiftInfo updatedLift = lift == null ? null : BreathingSpaceLiftInfo.builder()
            .event(lift.getEvent())
            .eventDescription(lift.getEventDescription())
            .expectedEnd(lift.getExpectedEnd() == null ? null : baseDate.plusDays(10))
            .build();

        return BreathingSpaceInfo.builder()
            .enter(updatedEnter)
            .lift(updatedLift)
            .build();
    }

    private static List<Element<GeneralApplication>> normaliseGeneralApplications(
        List<Element<GeneralApplication>> applications,
        LocalDateTime reference
    ) {
        if (applications == null) {
            return null;
        }
        AtomicInteger counter = new AtomicInteger();
        return applications.stream()
            .map(element -> new Element<>(
                element.getId(),
                element.getValue().toBuilder()
                    .generalAppSubmittedDateGAspec(reference.plusMinutes(counter.getAndIncrement()))
                    .generalAppDateDeadline(element.getValue().getGeneralAppDateDeadline() == null
                        ? null : reference.plusHours(1))
                    .applicationTakenOfflineDate(element.getValue().getApplicationTakenOfflineDate() == null
                        ? null : reference.plusHours(2))
                    .build()
            ))
            .collect(Collectors.toList());
    }

    private static List<Element<GeneralApplicationsDetails>> normaliseGeneralApplicationsDetails(
        List<Element<GeneralApplicationsDetails>> details,
        LocalDateTime reference
    ) {
        if (details == null) {
            return null;
        }
        AtomicInteger counter = new AtomicInteger();
        return details.stream()
            .map(element -> new Element<>(
                element.getId(),
                element.getValue().toBuilder()
                    .generalAppSubmittedDateGAspec(reference.plusMinutes(counter.getAndIncrement()))
                    .build()
            ))
            .collect(Collectors.toList());
    }

    private static List<Element<CaseNote>> normaliseCaseNotes(List<Element<CaseNote>> caseNotes, LocalDateTime reference) {
        if (caseNotes == null) {
            return null;
        }
        AtomicInteger counter = new AtomicInteger();
        return caseNotes.stream()
            .map(element -> new Element<>(
                element.getId(),
                CaseNote.builder()
                    .createdBy(element.getValue().getCreatedBy())
                    .note(element.getValue().getNote())
                    .createdOn(reference.plusMinutes(counter.getAndIncrement()))
                    .build()
            ))
            .collect(Collectors.toList());
    }
}
