package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.MediationAgreementDocument;
import uk.gov.hmcts.reform.civil.model.MediationSuccessful;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;
import uk.gov.hmcts.reform.civil.model.mediation.UploadMediationDocumentsForm;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public final class MediationWorkflowFixtures {

    private MediationWorkflowFixtures() {
    }

    private static final Document DUMMY_DOCUMENT = new Document(
        "http://dm-store/documents/fake-doc-id",
        "http://dm-store/documents/fake-doc-id/binary",
        "mediation-doc.pdf",
        "hash123",
        null,
        null
    );

    public static CaseData inMediation1v1() {
        return CaseDataTemplates.load("in-mediation-1v1");
    }

    public static CaseData inMediation1v2SameSolicitor() {
        return CaseDataTemplates.load("in-mediation-1v2-same-solicitor");
    }

    public static CaseData inMediation1v2DiffSolicitor() {
        return CaseDataTemplates.load("in-mediation-1v2-diff-solicitor");
    }

    public static CaseData inMediation2v1() {
        return CaseDataTemplates.load("in-mediation-2v1");
    }

    public static CaseData withMediationSuccessfulData(CaseData caseData) {
        MediationAgreementDocument agreementDoc = new MediationAgreementDocument();
        agreementDoc.setName("mediation-agreement.pdf");
        agreementDoc.setDocumentType(DocumentType.MEDIATION_AGREEMENT);
        agreementDoc.setDocument(DUMMY_DOCUMENT);
        agreementDoc.setDocumentUploadedDatetime(LocalDateTime.of(2026, 1, 20, 10, 0));

        MediationSuccessful mediationSuccessful = new MediationSuccessful();
        mediationSuccessful.setMediationSettlementAgreedAt(LocalDate.of(2026, 1, 20));
        mediationSuccessful.setMediationAgreement(agreementDoc);

        Mediation mediation = new Mediation();
        mediation.setMediationSuccessful(mediationSuccessful);

        return caseData.toBuilder()
            .mediation(mediation)
            .manageDocuments(new ArrayList<>())
            .build();
    }

    public static CaseData withMediationUnsuccessfulData(CaseData caseData,
                                                         MediationUnsuccessfulReason reason) {
        Mediation mediation = new Mediation();
        mediation.setUnsuccessfulMediationReason(reason.getValue());
        mediation.setMediationUnsuccessfulReasonsMultiSelect(List.of(reason));

        return caseData.toBuilder()
            .mediation(mediation)
            .build();
    }

    public static CaseData withUploadForm(CaseData caseData,
                                          String partyCode,
                                          List<MediationDocumentsType> docTypes) {
        UploadMediationDocumentsForm form = new UploadMediationDocumentsForm();
        form.setUploadMediationDocumentsPartyChosen(
            new DynamicList(
                DynamicListElement.dynamicElementFromCode(partyCode, partyCode),
                List.of(DynamicListElement.dynamicElementFromCode(partyCode, partyCode))
            )
        );
        form.setMediationDocumentsType(docTypes);

        if (docTypes.contains(MediationDocumentsType.NON_ATTENDANCE_STATEMENT)) {
            MediationNonAttendanceStatement statement = new MediationNonAttendanceStatement();
            statement.setYourName("Test Solicitor");
            statement.setDocumentDate(LocalDate.of(2026, 1, 15));
            statement.setDocument(DUMMY_DOCUMENT);
            form.setNonAttendanceStatementForm(List.of(element(statement)));
        }

        if (docTypes.contains(MediationDocumentsType.REFERRED_DOCUMENTS)) {
            MediationDocumentsReferredInStatement referred = new MediationDocumentsReferredInStatement();
            referred.setDocumentType("Witness statement");
            referred.setDocumentDate(LocalDate.of(2026, 1, 15));
            referred.setDocument(DUMMY_DOCUMENT);
            form.setDocumentsReferredForm(List.of(element(referred)));
        }

        return caseData.toBuilder()
            .uploadMediationDocumentsForm(form)
            .build();
    }

    public static CaseData withUploadFormFutureDates(CaseData caseData,
                                                     String partyCode,
                                                     List<MediationDocumentsType> docTypes) {
        UploadMediationDocumentsForm form = new UploadMediationDocumentsForm();
        form.setUploadMediationDocumentsPartyChosen(
            new DynamicList(
                DynamicListElement.dynamicElementFromCode(partyCode, partyCode),
                List.of(DynamicListElement.dynamicElementFromCode(partyCode, partyCode))
            )
        );
        form.setMediationDocumentsType(docTypes);

        LocalDate futureDate = LocalDate.now().plusDays(5);

        if (docTypes.contains(MediationDocumentsType.NON_ATTENDANCE_STATEMENT)) {
            MediationNonAttendanceStatement statement = new MediationNonAttendanceStatement();
            statement.setYourName("Test Solicitor");
            statement.setDocumentDate(futureDate);
            statement.setDocument(DUMMY_DOCUMENT);
            form.setNonAttendanceStatementForm(List.of(element(statement)));
        }

        if (docTypes.contains(MediationDocumentsType.REFERRED_DOCUMENTS)) {
            MediationDocumentsReferredInStatement referred = new MediationDocumentsReferredInStatement();
            referred.setDocumentType("Witness statement");
            referred.setDocumentDate(futureDate);
            referred.setDocument(DUMMY_DOCUMENT);
            form.setDocumentsReferredForm(List.of(element(referred)));
        }

        return caseData.toBuilder()
            .uploadMediationDocumentsForm(form)
            .build();
    }

    public static Document dummyDocument() {
        return DUMMY_DOCUMENT;
    }
}
