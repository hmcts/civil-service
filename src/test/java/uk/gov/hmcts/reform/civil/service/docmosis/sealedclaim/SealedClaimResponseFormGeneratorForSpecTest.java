package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
public class SealedClaimResponseFormGeneratorForSpecTest {

    @InjectMocks
    private SealedClaimResponseFormGeneratorForSpec generator;

    @Mock
    private RepresentativeService representativeService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;

    @Test
    public void contentCheck() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("case reference")
            .detailsOfWhyDoesYouDisputeTheClaim("why dispute the claim")
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQStatementOfTruth(
                                   StatementOfTruth.builder()
                                       .name("sot name")
                                       .role("sot role")
                                       .build()
                               )
                               .build())
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("applicant name")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant name")
                             .build())
            .respondent1ResponseDate(LocalDateTime.now())
            .build();
        SealedClaimResponseFormForSpec templateData = generator.getTemplateData(
            caseData);

        Assertions.assertEquals(caseData.getLegacyCaseReference(), templateData.getReferenceNumber());
        Assertions.assertEquals(
            caseData.getDetailsOfWhyDoesYouDisputeTheClaim(),
            templateData.getWhyDisputeTheClaim()
        );
        Assertions.assertEquals(
            caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getName(),
            templateData.getStatementOfTruth().getName()
        );
        Assertions.assertEquals(
            caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getRole(),
            templateData.getStatementOfTruth().getRole()
        );
    }

    @Test
    public void contentCheckMultiparty() {
        List<TimelineOfEvents> timelines = new ArrayList<>();
        timelines.add(TimelineOfEvents.builder()
                          .value(TimelineOfEventDetails.builder()
                                     .timelineDate(LocalDate.now()).timelineDescription("test timeline").build()).build());
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("case reference")
            .detailsOfWhyDoesYouDisputeTheClaim("why dispute the claim")
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQStatementOfTruth(
                                   StatementOfTruth.builder()
                                       .name("sot name")
                                       .role("sot role")
                                       .build()
                               )
                               .build())
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("applicant name")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant name")
                             .build())
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("defendant2 name")
                             .build())
            .respondent2Copy(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("defendant2 name")
                                 .build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent2ResponseDate(LocalDateTime.now().plusDays(3))
            .respondToAdmittedClaim(RespondToClaim.builder()
                                        .howMuchWasPaid(new BigDecimal(1000))
                                        .howWasThisAmountPaid(PaymentMethod.CREDIT_CARD)
                                        .whenWasThisAmountPaid(LocalDate.now()).build())
            .specResponseTimelineOfEvents(timelines)
            .build();
        SealedClaimResponseFormForSpec templateData = generator.getTemplateData(
            caseData);

        Assertions.assertEquals(caseData.getLegacyCaseReference(), templateData.getReferenceNumber());
        Assertions.assertEquals(
            caseData.getDetailsOfWhyDoesYouDisputeTheClaim(),
            templateData.getWhyDisputeTheClaim()
        );
        Assertions.assertEquals(
            caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getName(),
            templateData.getStatementOfTruth().getName()
        );
        Assertions.assertEquals(
            caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getRole(),
            templateData.getStatementOfTruth().getRole()
        );
    }
}
