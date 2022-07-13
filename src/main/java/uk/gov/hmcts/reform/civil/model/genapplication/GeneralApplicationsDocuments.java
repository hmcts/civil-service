package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class GeneralApplicationsDocuments implements MappableObject {

    private final List<Element<CaseDocument>> generalOrderDocument;
    private final List<Element<CaseDocument>> dismissalOrderDocument;
    private final List<Element<CaseDocument>> directionOrderDocument;
    private final List<Element<CaseDocument>> requestForInformationDocument;
    private final List<Element<CaseDocument>> hearingOrderDocument;
    private final List<Element<CaseDocument>> writtenRepSequentialDocument;
    private final List<Element<CaseDocument>> writtenRepConcurrentDocument;

    @JsonCreator
    GeneralApplicationsDocuments(@JsonProperty("generalOrderDocument")
                                     List<Element<CaseDocument>> generalOrderDocument,
                                 @JsonProperty("dismissalOrderDocument")
                                     List<Element<CaseDocument>> dismissalOrderDocument,
                                 @JsonProperty("directionOrderDocument")
                                     List<Element<CaseDocument>> directionOrderDocument,
                                 @JsonProperty("requestForInformationDocument")
                                     List<Element<CaseDocument>> requestForInformationDocument,
                                 @JsonProperty("hearingOrderDocument")
                                     List<Element<CaseDocument>> hearingOrderDocument,
                                 @JsonProperty("writtenRepSequentialDocument")
                                     List<Element<CaseDocument>> writtenRepSequentialDocument,
                                 @JsonProperty("writtenRepConcurrentDocument")
                                     List<Element<CaseDocument>> writtenRepConcurrentDocument) {
        this.generalOrderDocument = generalOrderDocument;
        this.dismissalOrderDocument = dismissalOrderDocument;
        this.directionOrderDocument = directionOrderDocument;
        this.requestForInformationDocument = requestForInformationDocument;
        this.hearingOrderDocument = hearingOrderDocument;
        this.writtenRepSequentialDocument = writtenRepSequentialDocument;
        this.writtenRepConcurrentDocument = writtenRepConcurrentDocument;
    }
}
