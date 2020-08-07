package uk.gov.hmcts.reform.unspec.model;

import lombok.Data;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.documents.Document;

import java.util.List;

@Data
public class ServedDocumentFiles {

    private List<Element<Document>> other;
    private List<Element<Document>> medicalReports;
    private List<Element<Document>> scheduleOfLoss;
    private List<Element<Document>> particularsOfClaim;
    private List<Element<Document>> certificateOfSuitability;
}
