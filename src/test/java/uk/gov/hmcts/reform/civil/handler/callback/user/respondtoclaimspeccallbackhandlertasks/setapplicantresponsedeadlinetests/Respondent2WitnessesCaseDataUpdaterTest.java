package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.Respondent2WitnessesCaseDataUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class Respondent2WitnessesCaseDataUpdaterTest {

    @InjectMocks
    private Respondent2WitnessesCaseDataUpdater updater;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        List<Element<Witness>> testWitness = wrapElements(Witness.builder().name("test witness").build());

        caseData = CaseData.builder()
                .respondent2DQ(Respondent2DQ.builder()
                        .respondent2DQWitnesses(Witnesses.builder()
                                .witnessesToAppear(YES)
                                .details(testWitness)
                                .build())
                        .build())
                .build();
    }

    @Test
    void shouldUpdateCaseDataWithWitnesses() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder()
                .respondent2DQ(caseData.getRespondent2DQ());

        updater.update(caseData, updatedData);

        Respondent2DQ updatedRespondent2DQ = updatedData.build().getRespondent2DQ();
        assertThat(updatedRespondent2DQ).isNotNull();
        Witnesses updatedWitnesses = updatedRespondent2DQ.getRespondent2DQWitnesses();
        assertThat(updatedWitnesses).isNotNull();
        assertThat(updatedWitnesses.getWitnessesToAppear()).isEqualTo(YES);
    }

    @Test
    void shouldHandleNullWitnesses() {
        caseData = caseData.toBuilder()
                .respondent2DQ(Respondent2DQ.builder().build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder()
                .respondent2DQ(caseData.getRespondent2DQ());

        updater.update(caseData, updatedData);

        Respondent2DQ updatedRespondent2DQ = updatedData.build().getRespondent2DQ();
        assertThat(updatedRespondent2DQ).isNotNull();
        assertThat(updatedRespondent2DQ.getRespondent2DQWitnesses()).isNull();
    }
}