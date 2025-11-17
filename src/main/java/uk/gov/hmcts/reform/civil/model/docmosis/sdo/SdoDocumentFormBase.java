package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;

/**
 * Base class for SDO Document Forms.
 * Contains common fields shared across all SDO document types to reduce code duplication.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"java:S1104"})
public abstract class SdoDocumentFormBase {

    protected LocalDate currentDate;
    protected String judgeName;
    protected String caseNumber;

    protected Party applicant1;
    protected Party respondent1;
    protected boolean hasApplicant2;
    protected Party applicant2;
    protected boolean hasRespondent2;
    protected Party respondent2;

    protected YesOrNo drawDirectionsOrderRequired;
    protected JudgementSum drawDirectionsOrder;

    protected LocationRefData hearingLocation;
    protected LocationRefData caseManagementLocation;

    protected String welshLanguageDescription;
    protected boolean hasNewDirections;
    protected boolean writtenByJudge;
}
