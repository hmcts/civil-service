package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceuploadhandlerbasetask;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public abstract class SetOptionsTask {

    protected static final String OPTION_APP1 = "Claimant 1 - ";
    protected static final String OPTION_APP2 = "Claimant 2 - ";
    protected static final String OPTION_APP_BOTH = "Claimants 1 and 2";
    protected static final String OPTION_DEF1 = "Defendant 1 - ";
    protected static final String OPTION_DEF2 = "Defendant 2 - ";
    protected static final String OPTION_DEF_BOTH = "Defendant 1 and 2";
    private List<Element<UploadEvidenceDocumentType>> additionalBundleDocs;
    protected final ObjectMapper objectMapper;

    public SetOptionsTask(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    abstract List<String> setPartyOptions(CaseData caseData);

    public CallbackResponse setOptions(CaseData caseData) {
        List<String> dynamicListOptions = setPartyOptions(caseData);
        additionalBundleDocs = new ArrayList<>();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        //Evidence upload will have different screen for Fast claims and Small claims.
        // We use show hide in CCD to do this, using utility field caseProgAllocatedTrack to hold the value of the claim track
        // for either spec claims (ResponseClaimTrack) or unspec claims (AllocatedTrack)
        if (caseData.getCaseAccessCategory().equals(UNSPEC_CLAIM)) {
            caseDataBuilder.caseProgAllocatedTrack(caseData.getAllocatedTrack().name());
        } else if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            caseDataBuilder.caseProgAllocatedTrack(caseData.getResponseClaimTrack());
        }
        caseDataBuilder.evidenceUploadOptions(DynamicList.fromList(dynamicListOptions));
        // was unable to null value properly in EvidenceUploadNotificationEventHandler after emails are sent,
        // so do it here if required.
        if (nonNull(caseData.getNotificationText()) && caseData.getNotificationText().equals("NULLED")) {
            caseDataBuilder.notificationText(null);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
