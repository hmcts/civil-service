package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class BilingualOrderDocumentServiceTest {

    @Mock
    private CivilStitchService civilStitchService;

    private BilingualOrderDocumentService service;

    @BeforeEach
    void setUp() {
        service = new BilingualOrderDocumentService(civilStitchService);
    }

    @Test
    void resolveSdo_returnsEnglishUrl_forEnglishParty() {
        CaseData caseData = caseDataWithSdoDocs();
        caseData.setClaimantBilingualLanguagePreference(Language.ENGLISH.toString());

        Optional<String> url = service.resolveSdoDocumentUrl(caseData, true, true);

        assertThat(url).contains("en-sdo-binary");
    }

    @Test
    void resolveSdo_returnsWelshUrl_forWelshParty() {
        CaseData caseData = caseDataWithSdoDocs();
        caseData.setClaimantBilingualLanguagePreference(Language.WELSH.toString());

        Optional<String> url = service.resolveSdoDocumentUrl(caseData, true, true);

        assertThat(url).contains("cy-sdo-binary");
    }

    @Test
    void resolveSdo_returnsBilingualUrl_forBothParty() {
        CaseData caseData = caseDataWithSdoDocs();
        caseData.setClaimantBilingualLanguagePreference(Language.BOTH.toString());
        caseData.getSystemGeneratedCaseDocuments().add(element(order(DocumentType.SDO_BILINGUAL_DOCUMENT, "bi-sdo-binary")));

        Optional<String> url = service.resolveSdoDocumentUrl(caseData, true, true);

        assertThat(url).contains("bi-sdo-binary");
    }

    @Test
    void stitchSdoIfRequired_stitchesWhenBothPreferenceAndDocsPresent() {
        CaseData caseData = caseDataWithSdoDocs();
        caseData.setCcdCaseReference(123L);
        caseData.setClaimantBilingualLanguagePreference(Language.BOTH.toString());
        CaseDocument stitched = order(DocumentType.SDO_BILINGUAL_DOCUMENT, "bi-sdo-binary");
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(),
                                                             eq(DocumentType.SDO_BILINGUAL_DOCUMENT), anyString()))
            .thenReturn(stitched);

        service.stitchSdoIfRequired(caseData, "Bearer token");

        verify(civilStitchService).generateStitchedCaseDocument(
            anyList(), eq("Bilingual_SDO.pdf"), eq(123L), eq(DocumentType.SDO_BILINGUAL_DOCUMENT), eq("Bearer token"));
        assertThat(caseData.getBilingualSDODocument()).isPresent();
    }

    @Test
    void stitchSdoIfRequired_skipsWhenNoBothParty() {
        CaseData caseData = caseDataWithSdoDocs();
        caseData.setClaimantBilingualLanguagePreference(Language.WELSH.toString());

        service.stitchSdoIfRequired(caseData, "Bearer token");

        verify(civilStitchService, never()).generateStitchedCaseDocument(
            any(), any(), any(), any(), any());
    }

    @Test
    void resolveFinalOrder_returnsWelshForDefendantWelshPreference() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCaseDataLiP(new CaseDataLiP().setRespondent1LiPResponse(
            new RespondentLiPResponse().setRespondent1ResponseLanguage(Language.WELSH.toString())));
        List<Element<CaseDocument>> orders = new ArrayList<>();
        orders.add(element(order(DocumentType.JUDGE_FINAL_ORDER, "en-order-binary")));
        orders.add(element(order(DocumentType.FINAL_ORDER_TRANSLATED_DOCUMENT, "cy-order-binary")));
        caseData.setFinalOrderDocumentCollection(orders);

        Optional<String> url = service.resolveFinalOrderDocumentUrl(caseData, false, true);

        assertThat(url).contains("cy-order-binary");
    }

    @Test
    void resolveHearingNotice_returnsBilingualUrl_forBothParty() {
        CaseData caseData = caseDataWithHearingDocs();
        caseData.setClaimantBilingualLanguagePreference(Language.BOTH.toString());
        caseData.getHearingDocuments().add(element(order(DocumentType.HEARING_NOTICE_BILINGUAL_DOCUMENT, "bi-hearing-binary")));

        Optional<String> url = service.resolveHearingNoticeDocumentUrl(caseData, true, true);

        assertThat(url).contains("bi-hearing-binary");
    }

    @Test
    void stitchHearingNoticeIfRequired_stitchesWhenBothPreferenceAndDocsPresent() {
        CaseData caseData = caseDataWithHearingDocs();
        caseData.setCcdCaseReference(123L);
        caseData.setClaimantBilingualLanguagePreference(Language.BOTH.toString());
        CaseDocument stitched = order(DocumentType.HEARING_NOTICE_BILINGUAL_DOCUMENT, "bi-hearing-binary");
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(),
                                                             eq(DocumentType.HEARING_NOTICE_BILINGUAL_DOCUMENT), anyString()))
            .thenReturn(stitched);

        service.stitchHearingNoticeIfRequired(caseData, "Bearer token");

        verify(civilStitchService).generateStitchedCaseDocument(
            anyList(), eq("Bilingual_Hearing_Notice.pdf"), eq(123L),
            eq(DocumentType.HEARING_NOTICE_BILINGUAL_DOCUMENT), eq("Bearer token"));
        assertThat(caseData.getBilingualHearingNoticeDocument()).isPresent();
    }

    private CaseData caseDataWithSdoDocs() {
        CaseData caseData = CaseData.builder().build();
        List<Element<CaseDocument>> docs = new ArrayList<>();
        docs.add(element(order(DocumentType.SDO_ORDER, "en-sdo-binary")));
        docs.add(element(order(DocumentType.SDO_TRANSLATED_DOCUMENT, "cy-sdo-binary")));
        caseData.setSystemGeneratedCaseDocuments(docs);
        return caseData;
    }

    private CaseData caseDataWithHearingDocs() {
        CaseData caseData = CaseData.builder().build();
        List<Element<CaseDocument>> hearingDocs = new ArrayList<>();
        hearingDocs.add(element(order(DocumentType.HEARING_FORM, "en-hearing-binary")));
        caseData.setHearingDocuments(hearingDocs);
        List<Element<CaseDocument>> welshDocs = new ArrayList<>();
        welshDocs.add(element(order(DocumentType.TRANSLATED_HEARING_NOTICE, "cy-hearing-binary")));
        caseData.setHearingDocumentsWelsh(welshDocs);
        return caseData;
    }

    private CaseDocument order(DocumentType type, String binaryUrl) {
        Document document = new Document();
        document.setDocumentBinaryUrl(binaryUrl);
        document.setDocumentUrl("url");
        document.setDocumentFileName("file.pdf");
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(type);
        caseDocument.setDocumentLink(document);
        caseDocument.setCreatedDatetime(LocalDateTime.now());
        return caseDocument;
    }
}
