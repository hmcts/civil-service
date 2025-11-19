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
    private List<Element<BundlingRequestDocument>> trialDocuments;
    @JsonProperty("statementsOfCaseDocuments")
    private List<Element<BundlingRequestDocument>> statementsOfCaseDocuments;
    @JsonProperty("directionsQuestionnaires")
    private List<Element<BundlingRequestDocument>> directionsQuestionnaires;
    @JsonProperty("particularsOfClaim")
    private List<Element<BundlingRequestDocument>> particularsOfClaim;
    @JsonProperty("ordersDocuments")
    private List<Element<BundlingRequestDocument>> ordersDocuments;
    @JsonProperty("claimant1WitnessStatements")
    private List<Element<BundlingRequestDocument>> claimant1WitnessStatements;
    @JsonProperty("claimant2WitnessStatements")
    private List<Element<BundlingRequestDocument>> claimant2WitnessStatements;
    @JsonProperty("defendant1WitnessStatements")
    private List<Element<BundlingRequestDocument>> defendant1WitnessStatements;
    @JsonProperty("defendant2WitnessStatements")
    private List<Element<BundlingRequestDocument>> defendant2WitnessStatements;
    @JsonProperty("claimant1ExpertEvidence")
    private List<Element<BundlingRequestDocument>> claimant1ExpertEvidence;
    @JsonProperty("claimant2ExpertEvidence")
    private List<Element<BundlingRequestDocument>> claimant2ExpertEvidence;
    @JsonProperty("defendant1ExpertEvidence")
    private List<Element<BundlingRequestDocument>> defendant1ExpertEvidence;
    @JsonProperty("defendant2ExpertEvidence")
    private List<Element<BundlingRequestDocument>> defendant2ExpertEvidence;
    @JsonProperty("jointStatementOfExperts")
    private List<Element<BundlingRequestDocument>> jointStatementOfExperts;
    @JsonProperty("claimant1DisclosedDocuments")
    private List<Element<BundlingRequestDocument>> claimant1DisclosedDocuments;
    @JsonProperty("claimant2DisclosedDocuments")
    private List<Element<BundlingRequestDocument>> claimant2DisclosedDocuments;
    @JsonProperty("defendant1DisclosedDocuments")
    private List<Element<BundlingRequestDocument>> defendant1DisclosedDocuments;
    @JsonProperty("defendant2DisclosedDocuments")
    private List<Element<BundlingRequestDocument>> defendant2DisclosedDocuments;
    @JsonProperty("claimant1CostsBudgets")
    private List<Element<BundlingRequestDocument>> claimant1CostsBudgets;
    @JsonProperty("claimant2CostsBudgets")
    private List<Element<BundlingRequestDocument>> claimant2CostsBudgets;
    @JsonProperty("defendant1CostsBudgets")
    private List<Element<BundlingRequestDocument>> defendant1CostsBudgets;
    @JsonProperty("defendant2CostsBudgets")
    private List<Element<BundlingRequestDocument>> defendant2CostsBudgets;
    @JsonProperty("systemGeneratedCaseDocuments")
    private List<Element<BundlingRequestDocument>> systemGeneratedCaseDocuments;
    @JsonProperty("applicant1")
    private Party applicant1;
    @JsonProperty("hasApplicant2")
    private boolean hasApplicant2;
    @JsonProperty("applicant2")
    private Party applicant2;
    @JsonProperty("respondant1")
    private Party respondent1;
    @JsonProperty("hasRespondant2")
    private boolean hasRespondant2;
    @JsonProperty("respondant2")
    private Party respondent2;
    @JsonProperty("hearingDate")
    private String hearingDate;
    @JsonProperty("ccdCaseReference")
    private Long ccdCaseReference;
}
