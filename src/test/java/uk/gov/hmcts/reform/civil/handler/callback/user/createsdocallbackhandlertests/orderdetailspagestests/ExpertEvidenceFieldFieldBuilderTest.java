package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.orderdetailspagestests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages.ExpertEvidenceFieldFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertEvidenceFieldFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private ExpertEvidenceFieldFieldBuilder fieldBuilder;

    @BeforeEach
    void setUp() {
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(14)))
                .thenReturn(LocalDate.now().plusDays(15));
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(42)))
                .thenReturn(LocalDate.now().plusDays(43));
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(49)))
                .thenReturn(LocalDate.now().plusDays(50));
    }

    @Test
    void shouldBuildFastTrackPersonalInjury() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackPersonalInjury personalInjury = caseData.getFastTrackPersonalInjury();

        assertThat(personalInjury).isNotNull();
        assertThat(personalInjury.getInput1()).isEqualTo("The Claimant has permission to rely upon the written expert evidence already uploaded to the Digital Portal with the " +
                "particulars of claim");
        assertThat(personalInjury.getInput2()).isEqualTo("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert directly and uploaded to " +
                "the Digital Portal by 4pm on");
        assertThat(personalInjury.getDate2()).isEqualTo(LocalDate.now().plusDays(15));
        assertThat(personalInjury.getInput3()).isEqualTo("The answers to the questions shall be answered by the Expert by");
        assertThat(personalInjury.getDate3()).isEqualTo(LocalDate.now().plusDays(43));
        assertThat(personalInjury.getInput4()).isEqualTo("and uploaded to the Digital Portal by the party who has asked the question by");
        assertThat(personalInjury.getDate4()).isEqualTo(LocalDate.now().plusDays(50));
    }
}