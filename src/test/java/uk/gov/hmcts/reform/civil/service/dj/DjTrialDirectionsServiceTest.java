package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.dj.DjSpecialistDirectionsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjTrialDirectionsServiceTest {

    private static final String JUDGE_NAME = "Recorder Blue";

    @Mock
    private WorkingDayIndicator workingDayIndicator;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private DjTrialDirectionsService service;

    @BeforeEach
    void setUp() {
        DjSpecialistDirectionsService specialistDirectionsService =
            new DjSpecialistDirectionsService(workingDayIndicator);
        service = new DjTrialDirectionsService(
            workingDayIndicator,
            deadlinesCalculator,
            specialistDirectionsService
        );

        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class)))
            .thenAnswer(invocation -> invocation.getArgument(0, LocalDate.class));
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), anyInt()))
            .thenAnswer(invocation -> {
                LocalDate date = invocation.getArgument(0, LocalDate.class);
                int days = invocation.getArgument(1, Integer.class);
                return date.plusDays(days);
            });
    }

    @Test
    void shouldPopulateTrialDirections() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.populateTrialDirections(builder, JUDGE_NAME);

        CaseData result = builder.build();

        TrialHearingJudgesRecital recital = result.getTrialHearingJudgesRecitalDJ();
        assertThat(recital).isNotNull();
        assertThat(recital.getJudgeNameTitle()).isEqualTo(JUDGE_NAME);

        TrialHearingDisclosureOfDocuments disclosure = result.getTrialHearingDisclosureOfDocumentsDJ();
        assertThat(disclosure.getDate2()).isEqualTo(LocalDate.now().plusWeeks(5));

        TrialOrderMadeWithoutHearingDJ orderWithoutHearing = result.getTrialOrderMadeWithoutHearingDJ();
        String expectedDeadline = LocalDate.now().plusDays(5)
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH));
        assertThat(orderWithoutHearing.getInput()).contains(expectedDeadline);

        SdoDJR2TrialCreditHire creditHire = result.getSdoDJR2TrialCreditHire();
        assertThat(creditHire.getDetailsShowToggle()).isEqualTo(List.of(AddOrRemoveToggle.ADD));

        SdoR2WelshLanguageUsage welshUsage = result.getSdoR2TrialWelshLanguageDJ();
        assertThat(welshUsage).isNotNull();
        assertThat(welshUsage.getDescription()).isEqualTo(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
    }
}
