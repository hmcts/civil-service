package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecEmailGeneratorTest {

    @Mock
    private ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appGen;

    @Mock
    private ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator resp1Gen;

    @Mock
    private ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator resp2Gen;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private ClaimContinuingOnlineSpecEmailGenerator generator;

    private CaseData representedCase;
    private final EmailDTO e1 = EmailDTO.builder().targetEmail("app").build();
    private final EmailDTO e2 = EmailDTO.builder().targetEmail("resp1").build();
    private final EmailDTO e3 = EmailDTO.builder().targetEmail("resp2").build();

    @BeforeEach
    void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);

        when(resp1Gen.buildEmailDTO(any(CaseData.class))).thenReturn(e2);

        representedCase = CaseData.builder()
                .applicant1Represented(YesOrNo.YES)
                .build();
    }

    @Test
    void shouldIncludeAppSol_andResp1_only_whenFlagFalse() {
        when(stateFlow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);

        when(appGen.buildEmailDTO(any(CaseData.class))).thenReturn(e1);

        Set<EmailDTO> result = generator.getPartiesToNotify(representedCase);

        assertThat(result).containsExactlyInAnyOrder(e1, e2);
    }

    @Test
    void shouldIncludeAppSol_andBothRespondents_whenFlagTrue() {
        when(stateFlow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        when(appGen.buildEmailDTO(any(CaseData.class))).thenReturn(e1);
        when(resp2Gen.buildEmailDTO(any(CaseData.class))).thenReturn(e3);

        Set<EmailDTO> result = generator.getPartiesToNotify(representedCase);

        assertThat(result).containsExactlyInAnyOrder(e1, e2, e3);
    }

    @Test
    void shouldSkipApplicant_whenUnrepresented_evenIfFlagTrue() {
        when(stateFlow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        when(resp2Gen.buildEmailDTO(any(CaseData.class))).thenReturn(e3);

        CaseData unrepresented = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                .build();

        Set<EmailDTO> result = generator.getPartiesToNotify(unrepresented);

        assertThat(result).containsExactlyInAnyOrder(e2, e3);
    }
}
