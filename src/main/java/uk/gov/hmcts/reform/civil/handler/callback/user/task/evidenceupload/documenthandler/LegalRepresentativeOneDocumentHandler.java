package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class LegalRepresentativeOneDocumentHandler<T> extends DocumentHandler<T> {

    private final DocumentCategory legalRepresentativeTwoDocumentCategory;
    protected final DocumentTypeBuilder<T> documentTypeBuilder;

    public LegalRepresentativeOneDocumentHandler(DocumentCategory documentCategory,
                                                 DocumentCategory legalRepresentativeTwoDocumentCategory,
                                                 EvidenceUploadType evidenceUploadType,
                                                 DocumentTypeBuilder<T> documentTypeBuilder,
                                                 UploadDocumentRetriever<T> uploadDocumentRetriever) {
        super(documentCategory, evidenceUploadType, uploadDocumentRetriever);
        this.legalRepresentativeTwoDocumentCategory = legalRepresentativeTwoDocumentCategory;
        this.documentTypeBuilder = documentTypeBuilder;
    }

    public CaseData copyLegalRep1ChangesToLegalRep2(CaseData caseData, CaseData caseDataBefore, CaseData.CaseDataBuilder builder) {
        List<Element<T>> evidenceDocsToCopy =
            compareAndCopy(getDocumentList(caseDataBefore),
                getDocumentList(caseData),
                getCorrespondingLegalRep2DocumentList(caseData));
        List<Element<T>> evidenceDocsToAdd =
            deepCopyUploadEvidenceDocumentType(evidenceDocsToCopy, legalRepresentativeTwoDocumentCategory.getCategoryId());
        addDocumentsToCopyToCaseData(builder, evidenceDocsToAdd);
        return builder.build();

    }

    protected <T> List<Element<T>> compareAndCopy(List<Element<T>> before,
                                                  List<Element<T>> after, List<Element<T>> target) {
        if (Objects.isNull(after) || after.isEmpty()) {
            return null;
        }
        List<Element<T>> different = new ArrayList<>();
        if (Objects.isNull(before)) {
            different = after;
        } else {
            List<UUID> ids = before.stream().map(Element::getId).toList();
            for (Element<T> element : after) {
                if (!ids.contains(element.getId())) {
                    different.add(element);
                }
            }
        }
        if (Objects.isNull(target)) {
            target = different;
        } else {
            target.addAll(different);
        }
        return target;
    }

    private List<Element<T>> deepCopyUploadEvidenceDocumentType(
        final List<Element<T>> documentListToCopy, String categoryId) {
        if (Objects.isNull(documentListToCopy)) {
            return null;
        }
        List<Element<T>> toAdd = new ArrayList<>();
        for (Element<T> from : documentListToCopy) {
            T type = documentTypeBuilder.buildElementTypeWithDocumentCopy(from.getValue(), categoryId);
            toAdd.add(ElementUtils.element(type));
        }
        return toAdd;
    }

    protected abstract void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<T>> evidenceDocsToAdd);

    protected abstract List<Element<T>> getCorrespondingLegalRep2DocumentList(CaseData caseData);

    public boolean shouldCopyDocumentsToLegalRep2() {
        return true;
    }

}
