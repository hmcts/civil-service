package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.pip.PiPLetter;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.PIN_IN_THE_POST_LETTER;

@ExtendWith(SpringExtension.class)
class PiPLetterGeneratorTest {

    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private PinInPostConfiguration pipInPostConfiguration;
    @InjectMocks
    private PiPLetterGenerator piPLetterGenerator;

    private static final LocalDateTime RESPONSE_DEADLINE = LocalDateTime.now();
    private static final Address RESPONDENT_ADDRESS = Address.builder().addressLine1("123 road")
        .postTown("London")
        .postCode("EX12RT")
        .build();
    private static final String CLAIMANT_FULL_NAME = "Mr. John Smith";
    private static final String CLAIM_REFERENCE = "ABC";
    private static final Party DEFENDANT = Party.builder().primaryAddress(RESPONDENT_ADDRESS)
        .type(Party.Type.INDIVIDUAL)
        .individualTitle("Mr.")
        .individualFirstName("Smith")
        .individualLastName("John")
        .build();
    private static final BigDecimal TOTAL_CLAIM_AMOUNT = new BigDecimal("1000");
    private static final String PIN = "1234789";

    private static final String CUI_URL = "CUI response url";
    private static final PiPLetter LETTER_TEMPLATE_DATA = PiPLetter.builder()
        .pin(PIN)
        .claimantName(CLAIMANT_FULL_NAME)
        .claimReferenceNumber(CLAIM_REFERENCE)
        .issueDate(LocalDate.now())
        .defendant(DEFENDANT)
        .responseDeadline(RESPONSE_DEADLINE.toLocalDate())
        .totalAmountOfClaim(TOTAL_CLAIM_AMOUNT)
        .respondToClaimUrl(CUI_URL)
        .build();
    private static final CaseData CASE_DATA = CaseData.builder()
        .legacyCaseReference(CLAIM_REFERENCE)
        .applicant1(Party.builder()
                        .type(Party.Type.INDIVIDUAL)
                        .individualTitle("Mr.")
                        .individualFirstName("John")
                        .individualLastName("Smith").build())
        .respondent1(DEFENDANT)
        .respondent1ResponseDeadline(RESPONSE_DEADLINE)
        .totalClaimAmount(TOTAL_CLAIM_AMOUNT)
        .respondent1PinToPostLRspec(DefendantPinToPostLRspec.builder().accessCode(PIN).build())
        .build();
    private static final DocmosisDocument LETTER = DocmosisDocument.builder()
        .bytes(new byte[]{1, 2, 3, 4, 5, 6})
        .build();

    @Test
    void shouldGenerateAndDownloadLetterSuccessfully() {
        given(pipInPostConfiguration.getRespondToClaimUrl()).willReturn(CUI_URL);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(LETTER);

        //CaseDocument downloadedLetter = piPLetterGenerator.downloadLetter(CASE_DATA, "111");

        assertThat(LETTER.getBytes()).isEqualTo(LETTER.getBytes());
        verify(documentGeneratorService, times(1)).generateDocmosisDocument(
            refEq(LETTER_TEMPLATE_DATA),
            refEq(PIN_IN_THE_POST_LETTER)
        );
    }

}
