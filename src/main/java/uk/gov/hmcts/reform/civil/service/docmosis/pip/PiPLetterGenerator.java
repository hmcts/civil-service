package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.pip.PiPLetter;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.PIN_IN_THE_POST_LETTER;

@AllArgsConstructor
@Service
public class PiPLetterGenerator implements TemplateDataGenerator<PiPLetter> {

    private final DocumentGeneratorService documentGeneratorService;
    private final PinInPostConfiguration pipInPostConfiguration;

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            PIN_IN_THE_POST_LETTER
        );
    }

    public byte[] downloadLetter(CaseData caseData) {
        DocmosisDocument pipLetter = generate(caseData);
        return pipLetter.getBytes();
    }

    @Override
    public PiPLetter getTemplateData(CaseData caseData) {
        return PiPLetter
            .builder()
            .pin(caseData.getRespondent1PinToPostLRspec().getAccessCode())
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1())
            .responseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate())
            .totalAmountOfClaim(caseData.getTotalClaimAmount())
            .respondToClaimUrl(pipInPostConfiguration.getRespondToClaimUrl())
            .issueDate(LocalDate.now())
            .build();
    }

}
