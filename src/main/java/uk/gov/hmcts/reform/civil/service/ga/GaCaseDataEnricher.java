package uk.gov.hmcts.reform.civil.service.ga;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dq.AssistedOrderCostDropdownList;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderCost;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.BeSpokeCostDetailText;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GaCaseDataEnricher {

    public CaseData enrich(CaseData caseData, GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData == null) {
            return caseData;
        }

        CaseData base = caseData != null ? caseData : CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = base.toBuilder();

        Optional.ofNullable(gaCaseData.getTranslatedDocuments()).ifPresent(builder::translatedDocuments);
        Optional.ofNullable(gaCaseData.getTranslatedDocumentsBulkPrint())
            .ifPresent(builder::translatedDocumentsBulkPrint);
        Optional.ofNullable(gaCaseData.getRespondent1LiPResponse()).ifPresent(builder::respondent1LiPResponse);
        Optional.ofNullable(gaCaseData.getApplicantBilingualLanguagePreference())
            .ifPresent(builder::applicantBilingualLanguagePreference);
        Optional.ofNullable(gaCaseData.getRespondentBilingualLanguagePreference())
            .ifPresent(builder::respondentBilingualLanguagePreference);
        Optional.ofNullable(gaCaseData.getAssistedOrderMakeAnOrderForCosts())
            .map(this::toAssistedOrderCostDetails)
            .ifPresent(builder::assistedOrderMakeAnOrderForCosts);
        Optional.ofNullable(gaCaseData.getAssistedOrderCostsBespoke())
            .map(this::toAssistedOrderCostsBespoke)
            .ifPresent(builder::assistedOrderCostsBespoke);
        Optional.ofNullable(gaCaseData.getFinalOrderSelection())
            .ifPresent(builder::finalOrderSelection);
        Optional.ofNullable(gaCaseData.getCaseManagementLocation())
            .map(this::toCaseLocationCivil)
            .ifPresent(builder::caseManagementLocation);

        return builder.build();
    }

    private AssistedOrderCostDetails toAssistedOrderCostDetails(AssistedOrderCost source) {
        if (source == null) {
            return null;
        }

        return AssistedOrderCostDetails.builder()
            .assistedOrderCostsFirstDropdownDate(source.getAssistedOrderCostsFirstDropdownDate())
            .assistedOrderAssessmentThirdDropdownDate(source.getAssistedOrderAssessmentThirdDropdownDate())
            .makeAnOrderForCostsYesOrNo(source.getMakeAnOrderForCostsYesOrNo())
            .makeAnOrderForCostsList(toCostEnum(source.getMakeAnOrderForCostsList()))
            .assistedOrderClaimantDefendantFirstDropdown(toCostEnum(source.getAssistedOrderCostsMakeAnOrderTopList()))
            .assistedOrderCostsFirstDropdownAmount(source.getAssistedOrderCostsFirstDropdownAmount())
            .assistedOrderAssessmentThirdDropdownAmount(source.getAssistedOrderAssessmentThirdDropdownAmount())
            .assistedOrderAssessmentSecondDropdownList1(toCostEnum(source.getAssistedOrderAssessmentSecondDropdownList1()))
            .assistedOrderAssessmentSecondDropdownList2(toCostEnum(source.getAssistedOrderAssessmentSecondDropdownList2()))
            .build();
    }

    private AssistedOrderCostDetails toAssistedOrderCostsBespoke(BeSpokeCostDetailText source) {
        if (source == null) {
            return null;
        }

        return AssistedOrderCostDetails.builder()
            .besPokeCostDetailsText(source.getDetailText())
            .build();
    }

    private CostEnums toCostEnum(AssistedOrderCostDropdownList value) {
        return Optional.ofNullable(value)
            .map(v -> CostEnums.valueOf(v.name()))
            .orElse(null);
    }

    private CaseLocationCivil toCaseLocationCivil(GACaseLocation source) {
        if (source == null) {
            return null;
        }

        return CaseLocationCivil.builder()
            .region(source.getRegion())
            .siteName(source.getSiteName())
            .baseLocation(source.getBaseLocation())
            .address(source.getAddress())
            .postcode(source.getPostcode())
            .build();
    }
}
