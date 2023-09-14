package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingCaseData {

    @JsonProperty("bundleConfiguration")
    public String bundleConfiguration;
    @JsonProperty("id")
    public long id;
    @JsonProperty("trialDocuments")
    private final List<Element<BundlingRequestDocument>> trialDocuments;
    @JsonProperty("statementsOfCaseDocuments")
    private final List<Element<BundlingRequestDocument>> statementsOfCaseDocuments;
    @JsonProperty("ordersDocuments")
    private final List<Element<BundlingRequestDocument>> ordersDocuments;
    @JsonProperty("claimant1WitnessStatements")
    private final List<Element<BundlingRequestDocument>> claimant1WitnessStatements;
    @JsonProperty("claimant2WitnessStatements")
    private final List<Element<BundlingRequestDocument>> claimant2WitnessStatements;
    @JsonProperty("defendant1WitnessStatements")
    private final List<Element<BundlingRequestDocument>> defendant1WitnessStatements;
    @JsonProperty("defendant2WitnessStatements")
    private final List<Element<BundlingRequestDocument>> defendant2WitnessStatements;
    @JsonProperty("claimant1ExpertEvidence")
    private final List<Element<BundlingRequestDocument>> claimant1ExpertEvidence;
    @JsonProperty("claimant2ExpertEvidence")
    private final List<Element<BundlingRequestDocument>> claimant2ExpertEvidence;
    @JsonProperty("defendant1ExpertEvidence")
    private final List<Element<BundlingRequestDocument>> defendant1ExpertEvidence;
    @JsonProperty("defendant2ExpertEvidence")
    private final List<Element<BundlingRequestDocument>> defendant2ExpertEvidence;
    @JsonProperty("jointStatementOfExperts")
    private final List<Element<BundlingRequestDocument>> jointStatementOfExperts;
    @JsonProperty("claimant1DisclosedDocuments")
    private final List<Element<BundlingRequestDocument>> claimant1DisclosedDocuments;
    @JsonProperty("claimant2DisclosedDocuments")
    private final List<Element<BundlingRequestDocument>> claimant2DisclosedDocuments;
    @JsonProperty("defendant1DisclosedDocuments")
    private final List<Element<BundlingRequestDocument>> defendant1DisclosedDocuments;
    @JsonProperty("defendant2DisclosedDocuments")
    private final List<Element<BundlingRequestDocument>> defendant2DisclosedDocuments;
    @JsonProperty("claimant1CostsBudgets")
    private final List<Element<BundlingRequestDocument>> claimant1CostsBudgets;
    @JsonProperty("claimant2CostsBudgets")
    private final List<Element<BundlingRequestDocument>> claimant2CostsBudgets;
    @JsonProperty("defendant1CostsBudgets")
    private final List<Element<BundlingRequestDocument>> defendant1CostsBudgets;
    @JsonProperty("defendant2CostsBudgets")
    private final List<Element<BundlingRequestDocument>> defendant2CostsBudgets;
    @JsonProperty("systemGeneratedCaseDocuments")
    private final List<Element<BundlingRequestDocument>> systemGeneratedCaseDocuments;
    @JsonProperty("courtLocation")
    private final String courtLocation;
    @JsonProperty("applicant1")
    private final Party applicant1;
    @JsonProperty("hasApplicant2")
    private final boolean hasApplicant2;
    @JsonProperty("applicant2")
    private final Party applicant2;
    @JsonProperty("respondant1")
    private final Party respondent1;
    @JsonProperty("hasRespondant2")
    private final boolean hasRespondant2;
    @JsonProperty("respondant2")
    private final Party respondent2;
    @JsonProperty("hearingDate")
    private final String hearingDate;
    @JsonProperty("ccdCaseReference")
    private final Long ccdCaseReference;
}
