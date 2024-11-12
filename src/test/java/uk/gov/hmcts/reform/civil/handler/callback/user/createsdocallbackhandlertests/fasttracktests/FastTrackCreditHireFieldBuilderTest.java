package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackCreditHireFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackCreditHireFieldBuilderTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private FastTrackCreditHireFieldBuilder fastTrackCreditHireFieldBuilder;

    @Test
    void shouldBuildFastTrackCreditHireFields() {
        LocalDate now = LocalDate.now();
        LocalDate date1 = now.plusWeeks(4);
        LocalDate date2 = now.plusWeeks(6);
        LocalDate date3 = now.plusWeeks(8);
        LocalDate date4 = now.plusWeeks(10);
        when(workingDayIndicator.getNextWorkingDay(date1)).thenReturn(date1);
        when(workingDayIndicator.getNextWorkingDay(date2)).thenReturn(date2);
        when(workingDayIndicator.getNextWorkingDay(date3)).thenReturn(date3);
        when(workingDayIndicator.getNextWorkingDay(date4)).thenReturn(date4);
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackCreditHireFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackCreditHire creditHire = caseData.getFastTrackCreditHire();
        assertThat(creditHire).isNotNull();
        assertThat(creditHire.getInput1()).isEqualTo("""
                If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's disclosure as ordered earlier in this Order must include:
                a) Evidence of all income from all sources for a period of 3 months prior to the commencement of hire until the earlier of:
                      i) 3 months after cessation of hire
                     ii) the repair or replacement of the claimant's vehicle
                b) Copies of all bank, credit card, and saving account statements for a period of 3 months prior to the commencement of hire until the earlier of:
                     i) 3 months after cessation of hire
                     ii) the repair or replacement of the claimant's vehicle
                c) Evidence of any loan, overdraft or other credit facilities available to the claimant.""");
        assertThat(creditHire.getInput2()).isEqualTo("""
                The claimant must upload to the Digital Portal a witness statement addressing
                a) the need to hire a replacement vehicle; and
                b) impecuniosity""");
        assertThat(creditHire.getDate1()).isEqualTo(date1);
        assertThat(creditHire.getInput3()).isEqualTo("A failure to comply with the paragraph above will result in the claimant being debarred from asserting need or relying on " +
                "impecuniosity as the case may be at the final hearing, save with permission of the Trial Judge.");
        assertThat(creditHire.getInput4()).isEqualTo("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no later than 4pm on");
        assertThat(creditHire.getDate2()).isEqualTo(date2);
        assertThat(creditHire.getInput5()).isEqualTo("If the parties fail to agree rates subject to liability and/or other issues pursuant to the paragraph above, each party may" +
                " rely upon written evidence by way of witness statement of one witness to provide evidence of basic hire rates available within the claimant's geographical " +
                "location, from a mainstream supplier, or a local reputable supplier if none is available.");
        assertThat(creditHire.getInput6()).isEqualTo("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
        assertThat(creditHire.getDate3()).isEqualTo(date3);
        assertThat(creditHire.getInput7()).isEqualTo("and the claimant's evidence in reply if so advised to be uploaded by 4pm on");
        assertThat(creditHire.getDate4()).isEqualTo(date4);
        assertThat(creditHire.getInput8()).isEqualTo("This witness statement is limited to 10 pages per party, including any appendices.");
    }
}