package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

class ConsentExtensionEventStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @InjectMocks
    private ConsentExtensionEventStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(5, 6, 7, 8);
    }

    @Test
    void supportsReturnsFalseWhenNoExtensionsPresent() {
        CaseData caseData = CaseData.builder().build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsSingleDefendantExtensionEvent() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("Defendant Ltd")
                .build())
            .respondent1TimeExtensionDate(LocalDateTime.of(2024, 3, 10, 9, 0))
            .respondentSolicitor1AgreedDeadlineExtension(LocalDate.of(2024, 3, 25))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getConsentExtensionFilingDefence()).hasSize(1);
        assertThat(history.getConsentExtensionFilingDefence().get(0).getEventSequence()).isEqualTo(5);
        assertThat(history.getConsentExtensionFilingDefence().get(0).getLitigiousPartyID()).isEqualTo(RESPONDENT_ID);
        assertThat(history.getConsentExtensionFilingDefence().get(0).getEventDetailsText())
            .isEqualTo("agreed extension date: 25 03 2024");
        assertThat(history.getConsentExtensionFilingDefence().get(0).getEventDetails().getAgreedExtensionDate())
            .isEqualTo("2024-03-25");
    }

    @Test
    void contributeAddsEventsForTwoDefendantsDifferentSolicitors() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("Defendant One")
                .build())
            .respondent1TimeExtensionDate(LocalDateTime.of(2024, 3, 12, 10, 0))
            .respondentSolicitor1AgreedDeadlineExtension(LocalDate.of(2024, 4, 1))
            .respondent2(Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("Defendant Two")
                .build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent2TimeExtensionDate(LocalDateTime.of(2024, 3, 13, 11, 0))
            .respondentSolicitor2AgreedDeadlineExtension(LocalDate.of(2024, 4, 2))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getConsentExtensionFilingDefence()).hasSize(2);
        assertThat(history.getConsentExtensionFilingDefence().get(0).getEventSequence()).isEqualTo(5);
        assertThat(history.getConsentExtensionFilingDefence().get(0).getEventDetailsText())
            .isEqualTo("Defendant: Defendant One has agreed extension: 01 04 2024");
        assertThat(history.getConsentExtensionFilingDefence().get(1).getEventSequence()).isEqualTo(6);
        assertThat(history.getConsentExtensionFilingDefence().get(1).getEventDetailsText())
            .isEqualTo("Defendant: Defendant Two has agreed extension: 02 04 2024");
        assertThat(history.getConsentExtensionFilingDefence().get(1).getLitigiousPartyID()).isEqualTo(RESPONDENT2_ID);
    }

    @Test
    void contributeAddsEventsForTwoDefendantsSameSolicitor() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Alex")
                .individualLastName("Smith")
                .build())
            .respondent1TimeExtensionDate(LocalDateTime.of(2024, 3, 5, 9, 0))
            .respondentSolicitor1AgreedDeadlineExtension(LocalDate.of(2024, 3, 28))
            .respondent2(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Jamie")
                .individualLastName("Roe")
                .build())
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent2TimeExtensionDate(LocalDateTime.of(2024, 3, 6, 9, 0))
            .respondentSolicitor2AgreedDeadlineExtension(LocalDate.of(2024, 3, 29))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getConsentExtensionFilingDefence()).hasSize(2);
        assertThat(history.getConsentExtensionFilingDefence().get(0).getEventSequence()).isEqualTo(5);
        assertThat(history.getConsentExtensionFilingDefence().get(0).getEventDetailsText())
            .isEqualTo("Defendant(s) have agreed extension: 28 03 2024");
        assertThat(history.getConsentExtensionFilingDefence().get(1).getEventSequence()).isEqualTo(6);
        assertThat(history.getConsentExtensionFilingDefence().get(1).getEventDetailsText())
            .isEqualTo("Defendant(s) have agreed extension: 29 03 2024");
    }
}
