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
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        ResponseDocument respondent1SpecDefenceResponseDocument = caseData.getRespondent1SpecDefenceResponseDocument();
        if (respondent1SpecDefenceResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1SpecDefenceResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent1ClaimDocument, "Defendant",
                    updatedCaseData.build().getRespondent1ResponseDate(),
                    DocumentType.DEFENDANT_DEFENCE
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1ClaimDocument,
                    DocCategory.DEF1_DEFENSE_DQ.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }
        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        if (respondent1DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1DQDraftDirections = respondent1DQ.getRespondent1DQDraftDirections();
            if (respondent1DQDraftDirections != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent1DQDraftDirections,
                    "Defendant",
                    updatedCaseData.build().getRespondent1ResponseDate(),
                    DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1DQDraftDirections,
                    DocCategory.DQ_DEF1.getValue()
                );
                defendantUploads.add(documentElement);
            }
            ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
            if (respondent2SpecDefenceResponseDocument != null) {
                uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
                if (respondent2ClaimDocument != null) {
                    Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent2ClaimDocument, DEF2,
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DEFENCE
                    );
                    assignCategoryId.assignCategoryIdToDocument(
                        respondent2ClaimDocument,
                        DocCategory.DEF2_DEFENSE_DQ.getValue()
                    );
                    defendantUploads.add(documentElement);
                }
            }
        } else {
            ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
            if (respondent2SpecDefenceResponseDocument != null) {
                uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
                if (respondent2ClaimDocument != null) {
                    Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent2ClaimDocument, DEF2,
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DEFENCE
                    );
                    assignCategoryId.assignCategoryIdToDocument(
                        respondent2ClaimDocument,
                        DocCategory.DEF2_DEFENSE_DQ.getValue()
                    );
                    CaseDocument copy = assignCategoryId
                        .copyCaseDocumentWithCategoryId(documentElement.getValue(), DocCategory.DQ_DEF2.getValue());
                    defendantUploads.add(documentElement);
                    if (Objects.nonNull(copy)) {
                        defendantUploads.add(ElementUtils.element(copy));
                    }
                }
            }
        }
        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        if (respondent2DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2DQDraftDirections = respondent2DQ.getRespondent2DQDraftDirections();
            if (respondent2DQDraftDirections != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent2DQDraftDirections,
                    DEF2,
                    updatedCaseData.build().getRespondent2ResponseDate(),
                    DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent2DQDraftDirections,
                    DocCategory.DQ_DEF2.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }
        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }
        // these documents are added to defendantUploads, if we do not remove/null the original,
        // case file view will show duplicate documents
        updatedCaseData.respondent1SpecDefenceResponseDocument(null);
        updatedCaseData.respondent2SpecDefenceResponseDocument(null);

    }

    public void populateRespondentResponseTypeSpecPaidStatus(CaseData caseData,
                                                              CaseData.CaseDataBuilder<?, ?> updated) {
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
            && caseData.getRespondToClaim().getHowMuchWasPaid() != null) {
            // CIV-208 howMuchWasPaid is pence, totalClaimAmount is pounds, hence the need for conversion
            int comparison = caseData.getRespondToClaim().getHowMuchWasPaid()
                .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                    RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
            } else {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                    RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
            }
        } else {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY)
                .build();
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired2())
                && caseData.getRespondToClaim2().getHowMuchWasPaid() != null) {
                // CIV-208 howMuchWasPaid is pence, totalClaimAmount is pounds, hence the need for conversion
                int comparison = caseData.getRespondToClaim2().getHowMuchWasPaid()
                    .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
                if (comparison < 0) {
                    updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
                } else {
                    updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
                }
            } else {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(null).build();
            }
        }
    }

    public void handleCourtLocationForRespondent1DQ(CaseData caseData,
                                                     Respondent1DQ.Respondent1DQBuilder dq,
                                                     CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = respondToClaimSpecUtilsCourtLocation.getCourtLocationDefendant1(caseData, callbackParams);
        // data for court location
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();

            dq.respondent1DQRequestedCourt(caseData.getRespondent1DQ()
                                               .getRespondToCourtLocation().toBuilder()
                                               .reasonForHearingAtSpecificCourt(
                                                   caseData.getRespondent1DQ()
                                                       .getRespondToCourtLocation()
                                                       .getReasonForHearingAtSpecificCourt())
                                               .responseCourtLocations(null)
                                               .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                                               .responseCourtCode(courtLocation.getCourtLocationCode()).build());
            dq.respondToCourtLocation(RequestedCourt.builder()
                                          .responseCourtLocations(null)
                                          .responseCourtCode(courtLocation.getCourtLocationCode())

                                          .build())
                .responseClaimCourtLocationRequired(YES);
        } else {
            dq.responseClaimCourtLocationRequired(NO);
        }
    }

    public void handleCourtLocationForRespondent2DQ(CaseData caseData, CaseData.CaseDataBuilder updatedCase,
                                                     Respondent2DQ.Respondent2DQBuilder dq,
                                                     CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = respondToClaimSpecUtilsCourtLocation.getCourtLocationDefendant2(caseData, callbackParams);
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            dq.respondent2DQRequestedCourt(caseData.getRespondent2DQ().getRespondToCourtLocation2().toBuilder()
                                               .responseCourtLocations(null)
                                               .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                                               .responseCourtCode(courtLocation.getCourtLocationCode()).build())
                .respondToCourtLocation2(RequestedCourt.builder()
                                             .responseCourtLocations(null)
                                             .responseCourtCode(courtLocation.getCourtLocationCode())
                                             .reasonForHearingAtSpecificCourt(
                                                 caseData.getRespondent2DQ().getRespondToCourtLocation2()
                                                     .getReasonForHearingAtSpecificCourt()
                                             )
                                             .build());
            updatedCase.responseClaimCourtLocation2Required(YES);
        } else {
            updatedCase.responseClaimCourtLocation2Required(NO);
        }
    }
}
