package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.Respondent1WitnessesCaseDataUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class Respondent1WitnessesCaseDataUpdaterTest {

    @InjectMocks
    private Respondent1WitnessesCaseDataUpdater updater;

    private CaseData caseData;

    @BeforeEach
    void setUp() {

        List<Element<Witness>> testWitness = wrapElements(Witness.builder().name("test witness").build());

        caseData = CaseData.builder()
                .respondent1DQ(Respondent1DQ.builder()
                        .respondent1DQWitnesses(Witnesses.builder()
                                .witnessesToAppear(YES)
                                .details(testWitness)
                                .build())
                        .build())
                .build();
    }

    @Test
    void shouldUpdateCaseDataWithWitnesses() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder()
                .respondent1DQ(caseData.getRespondent1DQ());

        updater.update(caseData, updatedData);

        Respondent1DQ updatedRespondent1DQ = updatedData.build().getRespondent1DQ();
        assertThat(updatedRespondent1DQ).isNotNull();
        Witnesses updatedWitnesses = updatedRespondent1DQ.getRespondent1DQWitnesses();
        assertThat(updatedWitnesses).isNotNull();
        assertThat(updatedWitnesses.getWitnessesToAppear()).isEqualTo(YES);
    }

    @Test
    void shouldHandleNullWitnesses() {
        caseData = caseData.toBuilder()
                .respondent1DQ(Respondent1DQ.builder().build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder()
                .respondent1DQ(caseData.getRespondent1DQ());

        updater.update(caseData, updatedData);

        Respondent1DQ updatedRespondent1DQ = updatedData.build().getRespondent1DQ();
        assertThat(updatedRespondent1DQ).isNotNull();
        assertThat(updatedRespondent1DQ.getRespondent1DQWitnesses()).isNull();
    }
}