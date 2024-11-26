package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.PartAdmittedByEitherRespondentsCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class PartAdmittedByEitherRespondentsCaseUpdaterTest {

    @InjectMocks
    private PartAdmittedByEitherRespondentsCaseUpdater updater;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldSetPartAdmittedByEitherRespondentsToYesForRespondent2() {
        CaseData caseData = CaseData.builder()
                .isRespondent2(YES)
                .specDefenceAdmitted2Required(YES)
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getPartAdmittedByEitherRespondents()).isEqualTo(YES);
    }

    @Test
    void shouldSetPartAdmittedByEitherRespondentsToYesForRespondent1() {
        CaseData caseData = CaseData.builder()
                .isRespondent1(YES)
                .specDefenceAdmittedRequired(YES)
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getPartAdmittedByEitherRespondents()).isEqualTo(YES);
    }

    @Test
    void shouldSetPartAdmittedByEitherRespondentsToNoWhenConditionsAreNotMet() {
        CaseData caseData = CaseData.builder()
                .isRespondent1(NO)
                .isRespondent2(NO)
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getPartAdmittedByEitherRespondents()).isEqualTo(NO);
    }
}