package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.dq.DocumentsToBeConsidered;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentsToBeConsideredSection extends DocumentsToBeConsidered {

    private static final String SECTION_HEADING_FORMAT = "%s documents to be considered";
    private static final String QUESTION_FORMAT = "Are there any documents the %s have that you want the court to consider?";
    private static final String DEFENDANTS = "Defendants";
    private static final String CLAIMANTS = "Claimants";
    private String sectionHeading;
    private String question;

    public static DocumentsToBeConsideredSection from(DocumentsToBeConsidered documentsToBeConsidered, boolean isDefendantSection) {
        if (documentsToBeConsidered == null) {
            return null;
        }
        String sectionHeading = isDefendantSection ? String.format(SECTION_HEADING_FORMAT, CLAIMANTS)
            : String.format(SECTION_HEADING_FORMAT, DEFENDANTS);

        String question = isDefendantSection ? String.format(QUESTION_FORMAT, CLAIMANTS.toLowerCase())
            : String.format(QUESTION_FORMAT, DEFENDANTS.toLowerCase());

        DocumentsToBeConsideredSection section = new DocumentsToBeConsideredSection();
        section.setHasDocumentsToBeConsidered(documentsToBeConsidered.getHasDocumentsToBeConsidered());
        section.setDetails(documentsToBeConsidered.getDetails());
        section.setSectionHeading(sectionHeading);
        section.setQuestion(question);
        return section;
    }
}
