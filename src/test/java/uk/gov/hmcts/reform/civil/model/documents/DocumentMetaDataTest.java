package uk.gov.hmcts.reform.civil.model.documents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class DocumentMetaDataTest {

    private final Document document = mock(Document.class);
    private final String description = "Some evidence";
    private final String dateUploaded = "2018-12-25";

    private DocumentMetaData documentWithMetadata =
        new DocumentMetaData(
            document,
            description,
            dateUploaded,
            "test"
        );

    @Test
    public void should_hold_onto_values() {

        assertEquals(document, documentWithMetadata.getDocument());
        assertEquals(description, documentWithMetadata.getDescription());
        assertEquals(dateUploaded, documentWithMetadata.getDateUploaded());
    }
}

