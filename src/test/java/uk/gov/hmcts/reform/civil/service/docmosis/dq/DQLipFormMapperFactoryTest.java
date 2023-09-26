package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class DQLipFormMapperFactoryTest {

    @Mock
    private CaseData caseData;
    @InjectMocks
    private DQLipFormMapperFactory factory;

    @Test
    void shouldReturnMapperForClaimant_whenCamundaProcessIsForClaimantResponse() {
        //Given
        given(caseData.getCurrentCamundaBusinessProcessName()).willReturn(DQLipFormMapperFactory.CLAIMANT_LIP_RESPONSE_PROCESS);
        //When
        DQLipFormMapper mapper = factory.getDQLipFormMapper(caseData);
        //Then
        assertThat(mapper).isInstanceOf(DQLipClaimantFormMapper.class);
    }

    @Test
    void shouldReturnMapperForDefendant_whenCamundaProcessIsForDefendantResponse() {
        //Given
        given(caseData.getCurrentCamundaBusinessProcessName()).willReturn("abracadabra");
        //When
        DQLipFormMapper mapper = factory.getDQLipFormMapper(caseData);
        //Then
        assertThat(mapper).isInstanceOf(DQLipDefendantFormMapper.class);
    }
}
