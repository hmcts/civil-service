package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionsToExpertsFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting questions to experts");
        updatedData.disposalHearingQuestionsToExperts(
            DisposalHearingQuestionsToExperts.builder()
                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                .build());
    }
}
