package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.LipDefenceFormParty;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimLipResponseForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.model.dq.HomeDetails;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_SPEC;

@Service
@RequiredArgsConstructor
public class SealedClaimLipResponseFormGenerator implements TemplateDataGenerator<SealedClaimLipResponseForm> {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;

    private static final Set<Party.Type> INDIVIDUAL_TYPES = EnumSet.of(Party.Type.INDIVIDUAL, Party.Type.SOLE_TRADER);
    // TODO look for existing constant
    private static final long DAYS_TO_PAY_IMMEDIATELY = 5;

    @Override
    public SealedClaimLipResponseForm getTemplateData(CaseData caseData) {
        SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder = SealedClaimLipResponseForm.builder()
            .generationDate(LocalDate.now())
            .responseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimant1(getPartyData(caseData.getApplicant1()))
            .defendant1(getPartyData(caseData.getRespondent1()))
            .defendant2(getPartyData(caseData.getRespondent2()))
            .mediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .partnerAndDependent(caseData.getRespondent1PartnerAndDependent())
            .currentlyWorking(caseData.getDefenceAdmitPartEmploymentTypeRequired() == YesOrNo.YES)
            .selfEmployment(caseData.getSpecDefendant1SelfEmploymentDetails());


        Optional.ofNullable(caseData.getResponseClaimAdmitPartEmployer())
            .map(Respondent1EmployerDetailsLRspec::getEmployerDetails)
            .map(ElementUtils::unwrapElements)
            .ifPresent(builder::employerDetails);


        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
            Optional.ofNullable(caseData.getRespondent1DQ())
                .map(Respondent1DQ::getRespondent1DQHomeDetails)
                .map(HomeDetails::getType)
                .ifPresent(type -> {
                    switch (type) {
                        case OWNED_HOME:
                            builder.whereTheyLive("Home they own or pay a mortgage on");
                            break;
                        case PRIVATE_RENTAL:
                            builder.whereTheyLive("Private rental");
                            break;
                        case ASSOCIATION_HOME:
                            builder.whereTheyLive("Council or housing association home");
                            break;
                        case JOINTLY_OWNED_HOME:
                            builder.whereTheyLive("Jointly-owned home (or jointly mortgaged home)");
                            break;
                        case OTHER:
                            builder.whereTheyLive("Other");
                            break;
                    }
                });
        }

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION:
                    if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                        == RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY) {
                        builder.payBy(LocalDate.now().plusDays(DAYS_TO_PAY_IMMEDIATELY))
                            .amountToPay(caseData.getTotalClaimAmount() + "");
                    } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                        == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN) {
                        builder.repaymentPlan(caseData.getRespondent1RepaymentPlan())
                            .payBy(caseData.getRespondent1RepaymentPlan()
                                       .finalPaymentBy(caseData.getTotalClaimAmount()));
                    } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                        == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
                        // TODO
                    }
                    break;
            }
        }

        return builder.build();
    }

    private LipDefenceFormParty getPartyData(Party party) {
        if (party == null) {
            return null;
        }
        LipDefenceFormParty.LipDefenceFormPartyBuilder builder = LipDefenceFormParty.builder()
            .name(party.getPartyName())
            .phone(party.getPartyPhone())
            .email(party.getPartyEmail())
            .primaryAddress(party.getPrimaryAddress());
        if (INDIVIDUAL_TYPES.contains(party.getType())) {
            builder.isIndividual(true);
            Stream.of(party.getIndividualDateOfBirth(), party.getSoleTraderDateOfBirth())
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(builder::dateOfBirth);
        } else {
            builder.isIndividual(false);
        }
        return builder.build();
    }

    public CaseDocument generate(CaseData caseData, String authorization) {
        SealedClaimLipResponseForm templateData = getTemplateData(caseData);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            DEFENDANT_RESPONSE_LIP_SPEC
        );
        String fileName = String.format(
            DEFENDANT_RESPONSE_LIP_SPEC.getDocumentTitle(),
            caseData.getLegacyCaseReference()
        );

        return documentManagementService.uploadDocument(
            authorization,
            new PDF(fileName, docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }
}
