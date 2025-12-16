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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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
        Witness witness = new Witness();
        witness.setFirstName("test witness");
        List<Element<Witness>> testWitness = wrapElements(witness);

        caseData = CaseDataBuilder.builder().build();
        Witnesses witnesses = new Witnesses();
        witnesses.setWitnessesToAppear(YES);
        witnesses.setDetails(testWitness);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQWitnesses(witnesses);
        caseData.setRespondent1DQ(respondent1DQ);
    }

    @Test
    void shouldUpdateCaseDataWithWitnesses() {
        updater.update(caseData);

        Respondent1DQ updatedRespondent1DQ = caseData.getRespondent1DQ();
        assertThat(updatedRespondent1DQ).isNotNull();
        Witnesses updatedWitnesses = updatedRespondent1DQ.getRespondent1DQWitnesses();
        assertThat(updatedWitnesses).isNotNull();
        assertThat(updatedWitnesses.getWitnessesToAppear()).isEqualTo(YES);
    }

    @Test
    void shouldHandleNullWitnesses() {
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        caseData.setRespondent1DQ(respondent1DQ);

        updater.update(caseData);

        Respondent1DQ updatedRespondent1DQ = caseData.getRespondent1DQ();
        assertThat(updatedRespondent1DQ).isNotNull();
        assertThat(updatedRespondent1DQ.getRespondent1DQWitnesses()).isNull();
    }
}
