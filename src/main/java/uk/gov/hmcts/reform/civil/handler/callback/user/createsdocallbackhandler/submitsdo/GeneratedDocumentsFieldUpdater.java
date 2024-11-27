package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Component
public class GeneratedDocumentsFieldUpdater implements SdoCaseDataFieldUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        CaseDocument document = caseData.getSdoOrderDocument();
        if (document != null) {
            log.debug("Adding generated document to systemGeneratedCaseDocuments for case {}", caseData.getCcdCaseReference());
            List<Element<CaseDocument>> generatedDocuments = caseData.getSystemGeneratedCaseDocuments();
            generatedDocuments.add(element(document));
            dataBuilder.systemGeneratedCaseDocuments(generatedDocuments);
        }
        dataBuilder.sdoOrderDocument(null);
    }
}
