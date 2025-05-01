package uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class GetRespondentsForDQGeneratorTest {

    @Mock
    private RepresentativeService representativeService;

    @InjectMocks
    private GetRespondentsForDQGenerator getRespondentsForDQGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnRespondentsWhenBusinessProcessIsClaimantResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
            .respondent2SameLegalRepresentative(NO)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
            .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .businessProcess(BusinessProcess.builder().camundaEvent("CLAIMANT_RESPONSE").build())
            .build();

        List<Party> respondents = getRespondentsForDQGenerator.getRespondents(caseData, "ONE");

        assertNotNull(respondents);
        assertEquals(2, respondents.size());
        assertEquals("Mr. Sole Trader", respondents.get(0).getName());
        assertEquals("Mr. John Rambo", respondents.get(1).getName());
    }

    @Test
    void shouldReturnRespondentWithSameLegalRepAndResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .respondentResponseIsSame(YES)
            .respondent2SameLegalRepresentative(YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .build();

        List<Party> respondents = getRespondentsForDQGenerator.getRespondents(caseData, "ONE");

        assertNotNull(respondents);
        assertEquals(2, respondents.size());
    }

    @Test
    void shouldReturnRespondentWithSameLegalRepDifferentResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .respondentResponseIsSame(NO)
            .respondent2SameLegalRepresentative(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .build();

        List<Party> respondents = getRespondentsForDQGenerator.getRespondents(caseData, "TWO");

        assertNotNull(respondents);
        assertEquals(1, respondents.size());
    }
}
