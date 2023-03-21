package uk.gov.hmcts.reform.civil.service.docmosis.aos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.aos.AcknowledgementOfClaimFormForSpec;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;

import java.time.LocalDate;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N10;

@ExtendWith(MockitoExtension.class)
class AcknowledgementOfClaimGeneratorForSpecTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = format(N10.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(ACKNOWLEDGEMENT_OF_SERVICE)
        .build();

    private final Representative representative = Representative.builder().organisationName("test org").build();

    @Mock
    private UnsecuredDocumentManagementService documentManagementService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private RepresentativeService representativeService;

    @InjectMocks
    private AcknowledgementOfClaimGeneratorForSpec generator;

    @BeforeEach
    void setup() {
        when(representativeService.getRespondent1Representative(any())).thenReturn(representative);
    }

    @Test
    void shouldGenerateAcknowledgementOfClaimForSpec_whenValidDataIsProvided() {
        // Given
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N10)))
            .willReturn(new DocmosisDocument(N10.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_SERVICE)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.getSolicitorReferences().setRespondentSolicitor2Reference("Not Provided");
        AcknowledgementOfClaimFormForSpec expectedDocmosisData = getExpectedFormData(caseData);

        // When
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        // Then
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_SERVICE));
        verify(documentGeneratorService)
            .generateDocmosisDocument(expectedDocmosisData, N10);
    }

    private AcknowledgementOfClaimFormForSpec getExpectedFormData(CaseData caseData) {
        return AcknowledgementOfClaimFormForSpec.builder()
            .caseName("Mr. John Rambo \nvs Mr. Sole Trader T/A Sole Trader co")
            .referenceNumber(LEGACY_CASE_REFERENCE)
            .submittedOn(LocalDate.now())
            .solicitorReferences(caseData.getSolicitorReferences())
            .issueDate(caseData.getIssueDate())
            .responseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate())
            .respondent(
                Party.builder()
                    .name(caseData.getRespondent1().getPartyName())
                    .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
                    .representative(representative)
                    .litigationFriendName(
                        ofNullable(caseData.getRespondent1LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse(""))
                    .build())
            .build();
    }
}
