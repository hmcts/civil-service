package uk.gov.hmcts.reform.unspec.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.unspec.model.docmosis.common.Applicant;
import uk.gov.hmcts.reform.unspec.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.unspec.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.unspec.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.unspec.model.dq.FurtherInformation;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class DirectionsQuestionnaireForm implements DocmosisData {

    @JsonProperty("courtseal")
    private final String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private final String caseName;
    private final String referenceNumber;
    private final SolicitorReferences solicitorReferences;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate submittedOn;
    private final Applicant applicant;
    private final FileDirectionsQuestionnaire fileDirectionsQuestionnaire;
    private final DisclosureOfElectronicDocuments disclosureOfElectronicDocuments;
    private final DisclosureOfNonElectronicDocuments disclosureOfNonElectronicDocuments;
    private final Experts experts;
    private final Witnesses witnesses;
    private final Hearing hearing;
    private final String hearingSupport;
    private final FurtherInformation furtherInformation;
    private final WelshLanguageRequirements welshLanguageRequirements;
    private final StatementOfTruth statementOfTruth;
    private final AllocatedTrack allocatedTrack;

}
