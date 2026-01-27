package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.SpecifiedParty;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimFormForSpec;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.TimelineEventDetailsDocmosis;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions.BREAK_DOWN_INTEREST;
import static uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions.SAME_RATE_INTEREST;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_1V2_DIFFERENT_SOL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_1V2_DIFFERENT_SOL_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_1V2_SAME_SOL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_2V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N2_2V1_LIP;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.formatCcdCaseReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class SealedClaimFormGeneratorForSpec implements TemplateDataGenerator<SealedClaimFormForSpec> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final RepresentativeService representativeService;
    private final InterestCalculator interestCalculator;
    public LocalDateTime localDateTime = LocalDateTime.now();
    private static final String END_OF_BUSINESS_DAY = "4pm, ";
    private final FeatureToggleService featureToggleService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        SealedClaimFormForSpec templateData;
        if (featureToggleService.isBulkClaimEnabled() && nonNull(caseData.getSdtRequestIdFromSdt())) {
            templateData = getTemplateDataBulkClaim(caseData);
        } else {
            templateData = getTemplateData(caseData);
        }
        DocmosisTemplates sealedTemplate = getSealedDocmosisTemplate(caseData);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            sealedTemplate
        );
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }

    @NotNull
    private DocmosisTemplates getSealedDocmosisTemplate(CaseData caseData) {
        DocmosisTemplates sealedTemplate;
        if (caseData.getApplicant2() != null) {
            if (YesOrNo.NO.equals(caseData.getSpecRespondent1Represented())) {
                sealedTemplate = N2_2V1_LIP;
            } else {
                sealedTemplate = N2_2V1;
            }
        } else if (caseData.getRespondent2() != null) {
            if (caseData.getRespondent2SameLegalRepresentative() != null
                && caseData.getRespondent2SameLegalRepresentative() == YES) {
                sealedTemplate = N2_1V2_SAME_SOL;
            } else {
                if (YesOrNo.NO.equals(caseData.getSpecRespondent1Represented())
                    || YesOrNo.NO.equals(caseData.getRespondent2Represented())) {
                    sealedTemplate = N2_1V2_DIFFERENT_SOL_LIP;
                } else {
                    sealedTemplate = N2_1V2_DIFFERENT_SOL;
                }
            }
        } else {
            sealedTemplate = N2;
        }
        return sealedTemplate;
    }

    private String getFileName(CaseData caseData) {
        return String.format(N1.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public SealedClaimFormForSpec getTemplateData(CaseData caseData) {
        Optional<SolicitorReferences> solicitorReferences = ofNullable(caseData.getSolicitorReferences());
        BigDecimal interest = interestCalculator.calculateInterest(caseData);

        return new SealedClaimFormForSpec()
            .setCcdCaseReference(formatCcdCaseReference(caseData))
            .setReferenceNumber(caseData.getLegacyCaseReference())
            .setCaseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .setApplicantExternalReference(solicitorReferences
                .map(SolicitorReferences::getApplicantSolicitor1Reference)
                .orElse(""))
            .setRespondentExternalReference(solicitorReferences
                .map(SolicitorReferences::getRespondentSolicitor1Reference)
                .orElse(""))
            .setIssueDate(caseData.getIssueDate())
            .setSubmittedOn(caseData.getSubmittedDate().toLocalDate())
            .setApplicants(getApplicants(caseData))
            .setRespondents(getRespondents(caseData))
            .setTimeline(getTimeLine(caseData))
            .setSameInterestRate(caseData.getInterestClaimOptions() != null
                ? caseData.getInterestClaimOptions().equals(SAME_RATE_INTEREST) + "" : null)
            .setBreakdownInterestRate(caseData.getInterestClaimOptions() != null
                ? caseData.getInterestClaimOptions().equals(BREAK_DOWN_INTEREST) + "" : null)
            .setInterestPerDayBreakdown(interestCalculator.getInterestPerDayBreakdown(caseData))
            .setTotalInterestAmount(interest != null ? interest.toString() : null)
            .setHowTheInterestWasCalculated(caseData.getInterestClaimOptions() != null
                ? caseData.getInterestClaimOptions().getDescription() : null)
            .setInterestRate(caseData.getSameRateInterestSelection() != null
                ? caseData.getSameRateInterestSelection().getDifferentRate() != null
                ? caseData.getSameRateInterestSelection().getDifferentRate() + "" :
                "8" : null)
            .setInterestExplanationText(caseData.getSameRateInterestSelection() != null
                ? caseData.getSameRateInterestSelection().getDifferentRate() != null
                ? caseData.getSameRateInterestSelection().getDifferentRateReason()
                : "The claimant reserves the right to claim interest under "
                + "Section 69 of the County Courts Act 1984" : null)
            .setInterestFromDate(getInterestFromDate(caseData))
            .setWhenAreYouClaimingInterestFrom(caseData.getInterestClaimFrom() != null
                ? caseData.getInterestClaimFrom().name()
                .equals("FROM_CLAIM_SUBMIT_DATE")
                ? "From the date the claim was submitted"
                : caseData.getInterestFromSpecificDateDescription() : null)
            .setInterestEndDate(localDateTime.toLocalDate())
            .setInterestEndDateDescription(caseData.getBreakDownInterestDescription() != null
                ? caseData.getBreakDownInterestDescription() + "" : null)
            .setTotalClaimAmount(caseData.getTotalClaimAmount() + "")
            .setInterestAmount(interest != null ? interest.toString() : null)
            .setClaimAmount(getClaimAmount(caseData))
            .setClaimFee(MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence())
                .toString())
            // Claim amount + interest + claim fees
            .setTotalAmountOfClaim(getTotalAmountOfClaim(caseData, interest))
            .setStatementOfTruth(caseData.getApplicantSolicitor1ClaimStatementOfTruth())
            .setDescriptionOfClaim(caseData.getDetailsOfClaim())
            .setApplicantRepresentativeOrganisationName(representativeService.getApplicantRepresentative(caseData)
                .getOrganisationName())
            .setDefendantResponseDeadlineDate(getResponseDeadline(caseData))
            .setClaimFixedCosts(caseData.getFixedCosts() != null ? caseData.getFixedCosts().getClaimFixedCosts() : null)
            .setFixedCostAmount(caseData.getFixedCosts() != null && caseData.getFixedCosts().getFixedCostAmount() != null
                                 ? MonetaryConversions.penniesToPounds(BigDecimal.valueOf(
                                     Integer.parseInt(caseData.getFixedCosts().getFixedCostAmount()))).toString()
                                 : (BigDecimal.valueOf(0)).toString())
            .setRespondentsOrgRegistered(getRespondentsOrgRegistered(caseData));
    }

    private LocalDate getInterestFromDate(CaseData caseData) {
        if (caseData.getInterestClaimFrom() == null) {
            return null;
        }
        return caseData.getInterestClaimFrom().equals(InterestClaimFromType.FROM_A_SPECIFIC_DATE)
            ? caseData.getInterestFromSpecificDate() : caseData.getSubmittedDate().toLocalDate();
    }

    private String getTotalAmountOfClaim(CaseData caseData, BigDecimal interest) {
        BigDecimal fixedCostAmount = caseData.getFixedCosts() != null
            && caseData.getFixedCosts().getFixedCostAmount() != null
            ? BigDecimal.valueOf(Integer.parseInt(caseData.getFixedCosts().getFixedCostAmount())) : null;

        BigDecimal totalClaimAmount = caseData.getTotalClaimAmount()
            .add(MonetaryConversions.penniesToPounds(caseData.getClaimFee()
                                                         .getCalculatedAmountInPence()));

        if (interest != null) {
            totalClaimAmount = totalClaimAmount.add(interest);
        }

        if (fixedCostAmount != null) {
            totalClaimAmount = totalClaimAmount.add(MonetaryConversions.penniesToPounds(fixedCostAmount));
        }

        return totalClaimAmount.toString();
    }

    public SealedClaimFormForSpec getTemplateDataBulkClaim(CaseData caseData) {
        Optional<SolicitorReferences> solicitorReferences = ofNullable(caseData.getSolicitorReferences());
        BigDecimal interest = null;
        if (caseData.getClaimInterest() == YesOrNo.YES) {
            interest = interestCalculator.calculateBulkInterest(caseData);
        }
        return new SealedClaimFormForSpec()
            .setCcdCaseReference(formatCcdCaseReference(caseData))
            .setReferenceNumber(caseData.getLegacyCaseReference())
            .setCaseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .setApplicantExternalReference(solicitorReferences.map(SolicitorReferences::getApplicantSolicitor1Reference).orElse(""))
            .setRespondentExternalReference(solicitorReferences.map(SolicitorReferences::getRespondentSolicitor1Reference).orElse(""))
            .setIssueDate(caseData.getIssueDate())
            .setSubmittedOn(caseData.getSubmittedDate().toLocalDate())
            .setApplicants(getApplicants(caseData))
            .setRespondents(getRespondents(caseData))
            .setSameInterestRate(caseData.getInterestClaimOptions() != null ? caseData.getInterestClaimOptions().equals(SAME_RATE_INTEREST) + "" : null)
            .setTotalInterestAmount(interest != null ? interest.toString() : null)
            .setInterestRate(
                interest != null ? "£" + caseData.getSameRateInterestSelection().getDifferentRate().toString() + " of interest per day" : null)
            .setInterestExplanationText("The claimant reserves the right to claim interest under Section 69 of the County Courts Act 1984")
            .setInterestFromDate(interest != null ? caseData.getInterestFromSpecificDate() : null)
            .setWhenAreYouClaimingInterestFrom("null for bulk") //clarify to remove
            .setInterestEndDate(interest != null ? LocalDate.now().minusDays(100) : null) //clarify to remove
            .setTotalClaimAmount(caseData.getTotalClaimAmount() + "")
            .setInterestAmount(interest != null ? interest.toString() : "0")
            .setClaimAmount(getClaimAmountBulk(caseData))
            .setClaimFee(MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence()).toString())
            // Claim amount + interest + claim fees
            .setTotalAmountOfClaim(getTotalAmountOfClaim(caseData, interest))
            .setStatementOfTruth(caseData.getApplicantSolicitor1ClaimStatementOfTruth())
            .setDescriptionOfClaim(caseData.getDetailsOfClaim())
            .setApplicantRepresentativeOrganisationName(representativeService.getApplicantRepresentative(caseData).getOrganisationName())
            .setClaimFixedCosts(caseData.getFixedCosts() != null ? caseData.getFixedCosts().getClaimFixedCosts() : null)
            .setFixedCostAmount(caseData.getFixedCosts() != null && caseData.getFixedCosts().getFixedCostAmount() != null
                                 ? MonetaryConversions.penniesToPounds(BigDecimal.valueOf(
                Integer.parseInt(caseData.getFixedCosts().getFixedCostAmount()))).toString()
                                 : (BigDecimal.valueOf(0)).toString());
    }

    private String getResponseDeadline(CaseData caseData) {
        String notificationDeadline = formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE);
        String responseDeadline = END_OF_BUSINESS_DAY + notificationDeadline;
        log.info("Response deadline: {} for caseID {} for claim form generation", responseDeadline, caseData.getCcdCaseReference());
        return responseDeadline;
    }

    private List<SpecifiedParty> getRespondents(CaseData caseData) {
        var respondent = caseData.getRespondent1();
        List<SpecifiedParty> parties = new ArrayList<>();
        parties.add(getRespondent(respondent, caseData, representativeService::getRespondent1Representative));
        if (caseData.getRespondent2() != null) {
            if (YES == caseData.getRespondent2SameLegalRepresentative()) {
                parties.add(getRespondent(caseData.getRespondent2(), caseData,
                    representativeService::getRespondent1Representative));
            } else {
                parties.add(getRespondent(caseData.getRespondent2(), caseData,
                    representativeService::getRespondent2Representative));
            }
        }
        return parties;
    }

    private SpecifiedParty getRespondent(
        Party respondent, CaseData caseData,
        Function<CaseData, Representative> representativeExtractor) {
        return new SpecifiedParty()
            .setName(respondent.getPartyName())
            .setPrimaryAddress(respondent.getPrimaryAddress())
            .setRepresentative(representativeExtractor.apply(caseData));
    }

    private List<TimelineEventDetailsDocmosis> getTimeLine(CaseData caseData) {
        if (caseData.getTimelineOfEvents() != null) {
            List<TimelineOfEvents> timelineOfEvents = caseData.getTimelineOfEvents();
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

    private List<ClaimAmountBreakupDetails> getClaimAmount(CaseData caseData) {
        if (caseData.getClaimAmountBreakup() != null) {
            List<ClaimAmountBreakup> claimAmountBreakup = caseData.getClaimAmountBreakup();
            List<ClaimAmountBreakupDetails> claimAmountBreakupDetails = new ArrayList<>();
            for (int index = 0; index < claimAmountBreakup.size(); index++) {
                ClaimAmountBreakupDetails claimAmountBreakupDetail
                    = new ClaimAmountBreakupDetails(
                    MonetaryConversions.penniesToPounds(claimAmountBreakup.get(index)
                        .getValue().getClaimAmount()),
                    claimAmountBreakup.get(index).getValue().getClaimReason()
                );
                claimAmountBreakupDetails.add(index, claimAmountBreakupDetail);
            }
            return claimAmountBreakupDetails;
        } else {
            return null;
        }
    }

    private List<ClaimAmountBreakupDetails> getClaimAmountBulk(CaseData caseData) {
        List<ClaimAmountBreakupDetails> claimAmountBreakupDetails = new ArrayList<>();
        ClaimAmountBreakupDetails claimAmountBreakupDetail = new ClaimAmountBreakupDetails(caseData.getTotalClaimAmount(), "Bulk claim");
        claimAmountBreakupDetails.add(claimAmountBreakupDetail);
        return claimAmountBreakupDetails;
    }

    private List<SpecifiedParty> getApplicants(CaseData caseData) {
        List<SpecifiedParty> parties = new ArrayList<>();
        parties.add(getApplicant(caseData.getApplicant1(), caseData));
        if (caseData.getApplicant2() != null) {
            parties.add(getApplicant(caseData.getApplicant2(), caseData));
        }
        return parties;
    }

    private SpecifiedParty getApplicant(Party applicant, CaseData caseData) {
        var representative = representativeService.getApplicantRepresentative(caseData);
        representative.setContactName(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());

        return new SpecifiedParty()
            .setName(applicant.getPartyName())
            .setPrimaryAddress(applicant.getPrimaryAddress())
            .setRepresentative(representative)
            .setIndividualDateOfBirth(applicant.getIndividualDateOfBirth() != null
                ? applicant.getIndividualDateOfBirth() : null);
    }

    private YesOrNo getRespondentsOrgRegistered(CaseData caseData) {
        if (YesOrNo.NO.equals(caseData.getRespondent1OrgRegistered())
            || YesOrNo.NO.equals(caseData.getRespondent2OrgRegistered())) {
            return YesOrNo.NO;
        }
        return YES;
    }
}
