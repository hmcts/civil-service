package uk.gov.hmcts.reform.civil.service.robotics.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoboticsManualOfflineSupportTest {

    private final RoboticsManualOfflineSupport support =
        new RoboticsManualOfflineSupport(new RoboticsEventTextFormatter());
    private final Logger logger = (Logger) LoggerFactory.getLogger(RoboticsManualOfflineSupport.class);
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void prepareTakenOfflineEventDetailsThrowsWhenUnspecDetailsAndOfflineDateMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);

        assertThatThrownBy(() -> support.prepareTakenOfflineEventDetails(caseData))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void prepareTakenOfflineEventDetailsFallsBackWhenUnspecDetailsMissing() {
        LocalDateTime offlineDate = LocalDateTime.of(2024, 7, 1, 9, 30);
        CaseData caseData = CaseDataBuilder.builder()
            .caseReference(1234567890123456L)
            .takenOfflineByStaffDate(offlineDate)
            .build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        caseData.setClaimProceedsInCaseman(null);

        assertThat(support.prepareTakenOfflineEventDetails(caseData))
            .isEqualTo("RPA Reason: Case taken offline by staff.");
        assertThat(caseData.getClaimProceedsInCaseman()).isNull();
        assertThat(logAppender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage())
                .contains("1234567890123456", "claimProceedsInCaseman", offlineDate.toString());
        });
    }

    @Test
    void prepareTakenOfflineEventDetailsThrowsWhenSpecDetailsMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);

        assertThatThrownBy(() -> support.prepareTakenOfflineEventDetails(caseData))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void prepareTakenOfflineEventDetailsHonoursProvidedDetails() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        ClaimProceedsInCasemanLR claimProceedsInCasemanLR = new ClaimProceedsInCasemanLR();
        claimProceedsInCasemanLR.setReason(ReasonForProceedingOnPaper.APPLICATION);
        claimProceedsInCasemanLR.setDate(LocalDate.of(2023, 3, 2));
        caseData.setClaimProceedsInCasemanLR(claimProceedsInCasemanLR);

        assertThat(support.prepareTakenOfflineEventDetails(caseData))
            .contains("APPLICATION")
            .contains("2023-03-02");
    }

    @Test
    void prepareTakenOfflineEventDetailsHonoursUnspecDetails() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        ClaimProceedsInCaseman claimProceedsInCaseman = new ClaimProceedsInCaseman();
        claimProceedsInCaseman.setReason(ReasonForProceedingOnPaper.OTHER);
        claimProceedsInCaseman.setOther("Manual review");
        claimProceedsInCaseman.setDate(LocalDate.of(2023, 6, 10));
        caseData.setClaimProceedsInCaseman(claimProceedsInCaseman);

        assertThat(support.prepareTakenOfflineEventDetails(caseData))
            .contains("Manual review")
            .contains("2023-06-10");
    }
}
