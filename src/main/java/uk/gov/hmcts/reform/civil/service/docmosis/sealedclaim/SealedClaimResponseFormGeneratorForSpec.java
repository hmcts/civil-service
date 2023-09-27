package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.SpecifiedParty;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.ResponseRepaymentDetailsForm;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.TimelineEventDetailsDocmosis;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGeneratorWithAuth;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1V1_INSTALLMENTS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1v1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1v2;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;

@Service
@RequiredArgsConstructor
public class SealedClaimResponseFormGeneratorForSpec implements TemplateDataGeneratorWithAuth<SealedClaimResponseFormForSpec> {

    private final RepresentativeService representativeService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final LocationRefDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;

    @Override
    public SealedClaimResponseFormForSpec getTemplateData(CaseData caseData, String authorisation) {
        String requestedCourt = null;
        StatementOfTruth statementOfTruth = null;
        if (caseData.getRespondent1DQ().getRespondent1DQRequestedCourt() != null) {
            requestedCourt = caseData.getRespondent1DQ().getRespondent1DQRequestedCourt().getCaseLocation().getBaseLocation();
            statementOfTruth = caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth();
        } else if (caseData.getRespondent2DQ().getRespondent2DQRequestedCourt() != null) {
            requestedCourt = caseData.getRespondent2DQ().getRespondent2DQRequestedCourt().getCaseLocation().getBaseLocation();
            statementOfTruth = caseData.getRespondent2DQ().getRespondent2DQStatementOfTruth();
        }
        List<LocationRefData> courtLocations = (locationRefDataService
            .getCourtLocationsByEpimmsId(
                authorisation,
                requestedCourt));

        Optional<LocationRefData> optionalCourtLocation = courtLocations.stream()
            .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
            .findFirst();
        String hearingCourtLocation = optionalCourtLocation
            .map(LocationRefData::getCourtName)
            .orElse(null);
        SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder
            = SealedClaimResponseFormForSpec.builder()
            .referenceNumber(caseData.getLegacyCaseReference())
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .whyDisputeTheClaim(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .hearingCourtLocation(hearingCourtLocation)
            .statementOfTruth(statementOfTruth);
        addRepaymentPlanDetails(builder, caseData);
        if (MultiPartyScenario.getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            builder.respondent1(getDefendant1v2ds(caseData));
        } else {
            builder.respondent1(getSpecifiedParty(
                caseData.getRespondent1(),
                representativeService.getRespondent1Representative(caseData)
            ));
            Optional.ofNullable(caseData.getRespondent2()).ifPresent(
                respondent2 ->
                    builder.respondent2(getSpecifiedParty(
                        respondent2,
                        representativeService.getRespondent2Representative(
                            caseData)
                    )));
        }

        Optional.ofNullable(caseData.getSolicitorReferences())
            .ifPresent(builder::solicitorReferences);

        if (isRespondent2(caseData) && !YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())) {
            Optional.ofNullable(caseData.getRespondent2ClaimResponseTypeForSpec())
                .map(RespondentResponseTypeSpec::getDisplayedValue)
                .ifPresent(builder::defendantResponse);
            builder.submittedOn(caseData.getRespondent2ResponseDate().toLocalDate());
        } else {
            Optional.ofNullable(caseData.getRespondent1ClaimResponseTypeForSpec())
                .map(RespondentResponseTypeSpec::getDisplayedValue)
                .ifPresent(builder::defendantResponse);
            builder.submittedOn(caseData.getRespondent1ResponseDate().toLocalDate());
        }

        if (caseData.getSpecResponseTimelineDocumentFiles() != null) {
            builder.timelineUploaded(true)
                .specResponseTimelineDocumentFiles(caseData.getSpecResponseTimelineDocumentFiles()
                                                       .getFile().getDocumentFileName());
        } else {
            builder.timelineUploaded(false)
                .timeline(getTimeLine(caseData));
        }

        if (caseData.getRespondent1SpecDefenceResponseDocument() != null) {
            builder.respondent1SpecDefenceResponseDocument(
                caseData.getRespondent1SpecDefenceResponseDocument().getFile().getDocumentFileName());
        }

        Stream.of(caseData.getRespondToClaim(), caseData.getRespondToAdmittedClaim())
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(response -> builder.poundsPaid(MonetaryConversions
                                                          .penniesToPounds(response.getHowMuchWasPaid()).toString())
                .paymentDate(response.getWhenWasThisAmountPaid())
                .paymentMethod(getPaymentMethod(response)));

        return builder.build();
    }

    private void addRepaymentPlanDetails(SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder, CaseData caseData) {
        if (featureToggleService.isPinInPostEnabled() && caseData.getRespondent2() == null) {
            builder.commonDetails(ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData));
        }
    }

    /**
     * We pass through this method twice, once for each defendant. Each time
     * we have to set the defendant who just answered.
     *
     * @param caseData a case data 1v2 with different solicitors and at least one of the defendants
     *                 has responded
     * @return which should be the defendant in the sealed claim form
     */
    private SpecifiedParty getDefendant1v2ds(CaseData caseData) {
        if (caseData.getRespondent1ResponseDate() == null
            || (caseData.getRespondent2ResponseDate() != null
            && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()))) {
            return getSpecifiedParty(
                caseData.getRespondent2(),
                representativeService.getRespondent2Representative(
                    caseData)
            );
        } else {
            return getSpecifiedParty(
                caseData.getRespondent1(),
                representativeService.getRespondent1Representative(
                    caseData)
            );
        }
    }

    private String getPaymentMethod(RespondToClaim response) {
        if (response.getHowWasThisAmountPaid() == PaymentMethod.OTHER) {
            return response.getHowWasThisAmountPaidOther();
        } else {
            return response.getHowWasThisAmountPaid().getHumanFriendly();
        }
    }

    private boolean isRespondent2(CaseData caseData) {
        return (caseData.getRespondent2ResponseDate() != null)
            && (caseData.getRespondent1ResponseDate() == null
            || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()));
    }

    private SpecifiedParty getSpecifiedParty(Party party, Representative representative) {
        SpecifiedParty.SpecifiedPartyBuilder builder = SpecifiedParty.builder()
            .name(party.getPartyName())
            .primaryAddress(party.getPrimaryAddress());
        Optional.ofNullable(representative).ifPresent(builder::representative);
        return builder.build();
    }

    private List<TimelineEventDetailsDocmosis> getTimeLine(CaseData caseData) {
        if (caseData.getSpecResponseTimelineOfEvents() != null) {
            List<TimelineOfEvents> timelineOfEvents = caseData.getSpecResponseTimelineOfEvents();
            List<TimelineEventDetailsDocmosis> timelineOfEventDetails = new ArrayList<>();
            for (int index = 0; index < timelineOfEvents.size(); index++) {
                TimelineOfEventDetails timelineOfEventDetail
                    = new TimelineOfEventDetails(
                    timelineOfEvents.get(index).getValue()
                        .getTimelineDate(),
                    timelineOfEvents.get(index).getValue().getTimelineDescription()
                );
                timelineOfEventDetails.add(index, new TimelineEventDetailsDocmosis(timelineOfEventDetail));
            }
            return timelineOfEventDetails;
        } else {
            return Collections.emptyList();
        }
    }

    public CaseDocument generate(CaseData caseData, String authorization) {
        SealedClaimResponseFormForSpec templateData = getTemplateData(caseData, authorization);
        DocmosisTemplates docmosisTemplate = getTemplate(caseData);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );
        String fileName = String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());

        return documentManagementService.uploadDocument(
            authorization,
            new PDF(fileName, docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }

    private DocmosisTemplates getTemplate(CaseData caseData) {
        if (caseData.getRespondent2() != null && YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())) {
            return DEFENDANT_RESPONSE_SPEC_SEALED_1v2;
        }
        return getDocmosisTemplateForSingleParty();
    }

    private DocmosisTemplates getDocmosisTemplateForSingleParty() {
        if (featureToggleService.isPinInPostEnabled()) {
            return DEFENDANT_RESPONSE_SPEC_SEALED_1V1_INSTALLMENTS;
        }
        return DEFENDANT_RESPONSE_SPEC_SEALED_1v1;
    }
}
