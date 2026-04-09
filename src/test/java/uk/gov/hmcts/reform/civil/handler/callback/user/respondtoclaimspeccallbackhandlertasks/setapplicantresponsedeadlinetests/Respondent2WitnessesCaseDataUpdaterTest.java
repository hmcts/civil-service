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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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
        Witness witness = new Witness();
        witness.setFirstName("test witness");
        List<Element<Witness>> testWitness = wrapElements(witness);

        caseData = CaseDataBuilder.builder().build();
        Witnesses witnesses = new Witnesses();
        witnesses.setWitnessesToAppear(YES);
        witnesses.setDetails(testWitness);
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQWitnesses(witnesses);
        caseData.setRespondent2DQ(respondent2DQ);
    }

    @Test
    void shouldUpdateCaseDataWithWitnesses() {

        updater.update(caseData);

        Respondent2DQ updatedRespondent2DQ = caseData.getRespondent2DQ();
        assertThat(updatedRespondent2DQ).isNotNull();
        Witnesses updatedWitnesses = updatedRespondent2DQ.getRespondent2DQWitnesses();
        assertThat(updatedWitnesses).isNotNull();
        assertThat(updatedWitnesses.getWitnessesToAppear()).isEqualTo(YES);
    }

    @Test
    void shouldHandleNullWitnesses() {
        caseData.setRespondent2DQ(new Respondent2DQ());

        updater.update(caseData);

        Respondent2DQ updatedRespondent2DQ = caseData.getRespondent2DQ();
        assertThat(updatedRespondent2DQ).isNotNull();
        assertThat(updatedRespondent2DQ.getRespondent2DQWitnesses()).isNull();
    }
}
