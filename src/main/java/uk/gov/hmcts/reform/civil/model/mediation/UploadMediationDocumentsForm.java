package uk.gov.hmcts.reform.civil.model.mediation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadMediationDocumentsForm {

    private DynamicList uploadMediationDocumentsPartyChosen;
    private List<MediationDocumentsType> mediationDocumentsType;
    private List<Element<MediationNonAttendanceStatement>> nonAttendanceStatementForm;
    private List<Element<MediationDocumentsReferredInStatement>> documentsReferredForm;
}
