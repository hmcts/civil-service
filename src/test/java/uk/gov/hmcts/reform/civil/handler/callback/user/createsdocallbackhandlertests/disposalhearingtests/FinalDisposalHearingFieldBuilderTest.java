package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.FinalDisposalHearingFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class FinalDisposalHearingFieldBuilderTest {

    @InjectMocks
    private FinalDisposalHearingFieldBuilder finalDisposalHearingFieldBuilder;

    @Test
    void shouldSetFinalDisposalHearing() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        finalDisposalHearingFieldBuilder.build(caseDataBuilder);
        CaseData caseData = caseDataBuilder.build();

        DisposalHearingFinalDisposalHearing expectedDisposalHearing = DisposalHearingFinalDisposalHearing.builder()
                .input("This claim will be listed for final disposal before a judge on the first available date after")
                .date(LocalDate.now().plusWeeks(16))
                .build();

        assertEquals(expectedDisposalHearing, caseData.getDisposalHearingFinalDisposalHearing());
    }
}