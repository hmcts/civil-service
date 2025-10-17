package uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.InterlocutoryJudgementDoc;
import uk.gov.hmcts.reform.civil.model.docmosis.InterlocutoryJudgementDocMapper;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InterlocutoryJudgementDocGeneratorTest {

    private static final String AUTHORISATION = "authorisation";
    private static final ObjectMapper GA_OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
    private final GaCaseDataEnricher gaCaseDataEnricher = new GaCaseDataEnricher();
    @Mock
    private InterlocutoryJudgementDocMapper mapper;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;

    private InterlocutoryJudgementDocGenerator generator;
    @Captor
    ArgumentCaptor<PDF> uploadDocumentArgumentCaptor;

    @BeforeEach
    public void setup() {
        generator = new InterlocutoryJudgementDocGenerator(mapper, documentManagementService, documentGeneratorService);
    }

    @Test
    void shouldGenerateInterlocutoryJudgementDoc() {

        //Given
        CaseData caseData = gaCaseData(builder -> builder);
        InterlocutoryJudgementDoc interlocutoryJudgementDoc = InterlocutoryJudgementDoc.builder().build();
        given(mapper.toInterlocutoryJudgementDoc(any())).willReturn(interlocutoryJudgementDoc);
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();

        given(documentGeneratorService.generateDocmosisDocument(
            any(InterlocutoryJudgementDoc.class),
            any()
        )).willReturn(
            docmosisDocument);

        //When
        generator.generateInterlocutoryJudgementDoc(caseData, AUTHORISATION);

        //Then
        verify(documentGeneratorService).generateDocmosisDocument(
            interlocutoryJudgementDoc,
            DocmosisTemplates.INTERLOCUTORY_JUDGEMENT_DOCUMENT
        );
        verify(documentManagementService).uploadDocument(
            eq(AUTHORISATION),
            uploadDocumentArgumentCaptor.capture()
        );

        PDF document = uploadDocumentArgumentCaptor.getValue();
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.INTERLOCUTORY_JUDGEMENT);
    }

    private CaseData gaCaseData(UnaryOperator<CaseData.CaseDataBuilder<?, ?>> customiser) {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(CaseDataBuilder.CASE_ID)
            .withGeneralAppParentCaseReference(CaseDataBuilder.PARENT_CASE_ID)
            .withLocationName("Nottingham County Court and Family Court (and Crown)")
            .withGaCaseManagementLocation(GACaseLocation.builder()
                                              .siteName("testing")
                                              .address("london court")
                                              .baseLocation("2")
                                              .postcode("BA 117")
                                              .build())
            .build();

        CaseData converted = GA_OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        CaseData enriched = gaCaseDataEnricher.enrich(converted, gaCaseData);

        return customiser.apply(enriched.toBuilder()).build();
    }
}
