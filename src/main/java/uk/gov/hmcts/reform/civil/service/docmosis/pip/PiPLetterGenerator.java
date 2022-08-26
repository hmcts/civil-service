package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import java.io.IOException;

@AllArgsConstructor
@Service
public class PiPLetterGenerator implements TemplateDataGenerator<PiPLetterGenerator> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public DocmosisDocument generate(CaseData caseData, String authentication){

    }

    @Override
    public PiPLetterGenerator getTemplateData(CaseData caseData) throws IOException {
        return null;
    }
}
