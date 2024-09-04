package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;

@Component
@RequiredArgsConstructor
public class RespondToClaimSpecDocumentHandler {

    private static final String DEF2 = "Defendant 2";
    private final AssignCategoryId assignCategoryId;
    private final RespondToClaimSpecUtilsCourtLocation respondToClaimSpecUtilsCourtLocation;

    public void assembleResponseDocumentsSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();
        processRespondent1Documents(caseData, updatedCaseData, defendantUploads);
        processRespondent2Documents(caseData, updatedCaseData, defendantUploads);

        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }
        // these documents are added to defendantUploads, if we do not remove/null the original,
        // case file view will show duplicate documents
        updatedCaseData.respondent1SpecDefenceResponseDocument(null);
        updatedCaseData.respondent2SpecDefenceResponseDocument(null);
    }

    private void processRespondent1Documents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        processResponseDocument(caseData.getRespondent1SpecDefenceResponseDocument(), "Defendant", updatedCaseData, DocCategory.DEF1_DEFENSE_DQ, defendantUploads);
        processDQDocument(caseData.getRespondent1DQ(), updatedCaseData, defendantUploads);
    }

    private void processRespondent2Documents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        processResponseDocument(caseData.getRespondent2SpecDefenceResponseDocument(), DEF2, updatedCaseData, DocCategory.DEF2_DEFENSE_DQ, defendantUploads);
        processDQDocument(caseData.getRespondent2DQ(), updatedCaseData, defendantUploads);
    }

    private void processResponseDocument(ResponseDocument responseDocument, String party, CaseData.CaseDataBuilder<?, ?> updatedCaseData, DocCategory docCategory, List<Element<CaseDocument>> defendantUploads) {
        if (responseDocument != null && responseDocument.getFile() != null) {
            Element<CaseDocument> documentElement = buildElemCaseDocument(
                responseDocument.getFile(), party,
                updatedCaseData.build().getRespondent1ResponseDate(),
                DocumentType.DEFENDANT_DEFENCE
            );
            assignCategoryId.assignCategoryIdToDocument(responseDocument.getFile(), docCategory.getValue());
            defendantUploads.add(documentElement);
        }
    }

    private void processDQDocument(Respondent1DQ dq, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        if (dq != null && dq.getRespondent1DQDraftDirections() != null) {
            Element<CaseDocument> documentElement = buildElemCaseDocument(
                dq.getRespondent1DQDraftDirections(),
                "Defendant",
                updatedCaseData.build().getRespondent1ResponseDate(),
                DocumentType.DEFENDANT_DRAFT_DIRECTIONS
            );
            assignCategoryId.assignCategoryIdToDocument(dq.getRespondent1DQDraftDirections(), DocCategory.DQ_DEF1.getValue());
            defendantUploads.add(documentElement);
        }
    }

    private void processDQDocument(Respondent2DQ dq, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        if (dq != null && dq.getRespondent2DQDraftDirections() != null) {
            Element<CaseDocument> documentElement = buildElemCaseDocument(
                dq.getRespondent2DQDraftDirections(),
                RespondToClaimSpecDocumentHandler.DEF2,
                updatedCaseData.build().getRespondent2ResponseDate(),
                DocumentType.DEFENDANT_DRAFT_DIRECTIONS
            );
            assignCategoryId.assignCategoryIdToDocument(dq.getRespondent2DQDraftDirections(), DocCategory.DQ_DEF2.getValue());
            defendantUploads.add(documentElement);
        }
    }

    public void populateRespondentResponseTypeSpecPaidStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated) {
        RespondentResponseTypeSpecPaidStatus paymentStatus = getPaymentStatus(caseData.getDefenceRouteRequired(), caseData.getRespondToClaim().getHowMuchWasPaid(), caseData.getTotalClaimAmount());
        updated.respondent1ClaimResponsePaymentAdmissionForSpec(paymentStatus).build();

        if (YES.equals(caseData.getIsRespondent2())) {
            paymentStatus = getPaymentStatus(caseData.getDefenceRouteRequired2(), caseData.getRespondToClaim2().getHowMuchWasPaid(), caseData.getTotalClaimAmount());
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(paymentStatus).build();
        }
    }

    private RespondentResponseTypeSpecPaidStatus getPaymentStatus(String defenceRouteRequired, BigDecimal howMuchWasPaid, BigDecimal totalClaimAmount) {
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(defenceRouteRequired) && howMuchWasPaid != null) {
            int comparison = howMuchWasPaid.compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(totalClaimAmount)));
            return comparison < 0 ? RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT : RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT;
        } else {
            return RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY;
        }
    }

    public void handleCourtLocationForRespondent1DQ(CaseData caseData, Respondent1DQ.Respondent1DQBuilder dq, CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = respondToClaimSpecUtilsCourtLocation.getCourtLocationDefendant1(caseData, callbackParams);
        updateCourtLocation(optCourtLocation, dq::respondToCourtLocation, caseData.getRespondent1DQ().getRespondToCourtLocation(), dq::responseClaimCourtLocationRequired);
    }

    public void handleCourtLocationForRespondent2DQ(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase, Respondent2DQ.Respondent2DQBuilder dq, CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = respondToClaimSpecUtilsCourtLocation.getCourtLocationDefendant2(caseData, callbackParams);
        updateCourtLocation(optCourtLocation, dq::respondToCourtLocation2, caseData.getRespondent2DQ().getRespondToCourtLocation2(), updatedCase::responseClaimCourtLocation2Required);
    }

    private void updateCourtLocation(Optional<LocationRefData> optCourtLocation, java.util.function.Consumer<RequestedCourt> setRequestedCourt, RequestedCourt requestedCourt, java.util.function.Consumer<YesOrNo> setClaimCourtLocationRequired) {
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            setRequestedCourt.accept(requestedCourt.toBuilder()
                                         .responseCourtLocations(null)
                                         .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                                         .responseCourtCode(courtLocation.getCourtLocationCode())
                                         .build());
            setClaimCourtLocationRequired.accept(YES);
        } else {
            setClaimCourtLocationRequired.accept(NO);
        }
    }
}
