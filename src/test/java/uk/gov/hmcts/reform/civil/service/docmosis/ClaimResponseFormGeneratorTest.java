package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.docmosis.ClaimResponseForm;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
public class ClaimResponseFormGeneratorTest {

    @InjectMocks
    private ClaimResponseFormGenerator generator;
    @Mock
    private RepresentativeService representativeService;

    @Test
    public void whenRespondent1_getResponse1() throws IOException {
        String caseRef = "case ref";
        LocalDate issued = LocalDate.now().minusDays(23);
        LocalDateTime responseDate = LocalDateTime.now();

        LitigationFriend litigationFriend = LitigationFriend.builder()
            .fullName("litigation friend full name")
            .build();
        Party respondent = Party.builder()
            .partyName("Party name")
            .primaryAddress(Address.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .legacyCaseReference(caseRef)
            .issueDate(issued)
            .respondent1ResponseDate(responseDate)
            .respondent1LitigationFriend(litigationFriend)
            .respondent1(respondent)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .detailsOfWhyDoesYouDisputeTheClaim("reason to dispute")
            .uiStatementOfTruth(StatementOfTruth.builder()
                                    .name("sot name")
                                    .role("sot role")
                                    .build())
            .build();

        Representative representative = Mockito.mock(Representative.class);
        Mockito.when(representativeService.getRespondent1Representative(caseData))
            .thenReturn(representative);

        ClaimResponseForm form = generator.getTemplateData(caseData);

        Assert.assertEquals(caseRef, form.getReferenceNumber());
        Assert.assertEquals(issued, form.getIssueDate());
        Assert.assertEquals(responseDate, form.getSubmittedOn());
        Assert.assertEquals(respondent.getPartyName(), form.getRespondent().getName());
        Assert.assertEquals(respondent.getPrimaryAddress(), form.getRespondent().getPrimaryAddress());
        Assert.assertEquals(representative, form.getRespondent().getRepresentative());
        Assert.assertEquals(litigationFriend.getFullName(), form.getRespondent().getLitigationFriendName());
        Assert.assertEquals(caseData.getRespondent1ClaimResponseTypeForSpec().getDisplayedValue(),
                            form.getDefendantResponse());
        Assert.assertEquals(form.getWhyDisputeTheClaim(), caseData.getDetailsOfWhyDoesYouDisputeTheClaim());
        Assert.assertEquals(form.getStatementOfTruth(), caseData.getUiStatementOfTruth());

        Mockito.verify(representativeService).getRespondent1Representative(caseData);
    }
}
