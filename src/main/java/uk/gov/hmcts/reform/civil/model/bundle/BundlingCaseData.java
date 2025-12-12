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
    List<Element<BundlingRequestDocument>> trialDocuments;
    @JsonProperty("statementsOfCaseDocuments")
    List<Element<BundlingRequestDocument>> statementsOfCaseDocuments;
    @JsonProperty("directionsQuestionnaires")
    List<Element<BundlingRequestDocument>> directionsQuestionnaires;
    @JsonProperty("particularsOfClaim")
    List<Element<BundlingRequestDocument>> particularsOfClaim;
    @JsonProperty("ordersDocuments")
    List<Element<BundlingRequestDocument>> ordersDocuments;
    @JsonProperty("claimant1WitnessStatements")
    List<Element<BundlingRequestDocument>> claimant1WitnessStatements;
    @JsonProperty("claimant2WitnessStatements")
    List<Element<BundlingRequestDocument>> claimant2WitnessStatements;
    @JsonProperty("defendant1WitnessStatements")
    List<Element<BundlingRequestDocument>> defendant1WitnessStatements;
    @JsonProperty("defendant2WitnessStatements")
    List<Element<BundlingRequestDocument>> defendant2WitnessStatements;
    @JsonProperty("claimant1ExpertEvidence")
    List<Element<BundlingRequestDocument>> claimant1ExpertEvidence;
    @JsonProperty("claimant2ExpertEvidence")
    List<Element<BundlingRequestDocument>> claimant2ExpertEvidence;
    @JsonProperty("defendant1ExpertEvidence")
    List<Element<BundlingRequestDocument>> defendant1ExpertEvidence;
    @JsonProperty("defendant2ExpertEvidence")
    List<Element<BundlingRequestDocument>> defendant2ExpertEvidence;
    @JsonProperty("jointStatementOfExperts")
    List<Element<BundlingRequestDocument>> jointStatementOfExperts;
    @JsonProperty("claimant1DisclosedDocuments")
    List<Element<BundlingRequestDocument>> claimant1DisclosedDocuments;
    @JsonProperty("claimant2DisclosedDocuments")
    List<Element<BundlingRequestDocument>> claimant2DisclosedDocuments;
    @JsonProperty("defendant1DisclosedDocuments")
    List<Element<BundlingRequestDocument>> defendant1DisclosedDocuments;
    @JsonProperty("defendant2DisclosedDocuments")
    List<Element<BundlingRequestDocument>> defendant2DisclosedDocuments;
    @JsonProperty("claimant1CostsBudgets")
    List<Element<BundlingRequestDocument>> claimant1CostsBudgets;
    @JsonProperty("claimant2CostsBudgets")
    List<Element<BundlingRequestDocument>> claimant2CostsBudgets;
    @JsonProperty("defendant1CostsBudgets")
    List<Element<BundlingRequestDocument>> defendant1CostsBudgets;
    @JsonProperty("defendant2CostsBudgets")
    List<Element<BundlingRequestDocument>> defendant2CostsBudgets;
    @JsonProperty("systemGeneratedCaseDocuments")
    List<Element<BundlingRequestDocument>> systemGeneratedCaseDocuments;
    @JsonProperty("applicant1")
    Party applicant1;
    @JsonProperty("hasApplicant2")
    boolean hasApplicant2;
    @JsonProperty("applicant2")
    Party applicant2;
    @JsonProperty("respondant1")
    Party respondent1;
    @JsonProperty("hasRespondant2")
    boolean hasRespondant2;
    @JsonProperty("respondant2")
    Party respondent2;
    @JsonProperty("hearingDate")
    String hearingDate;
    @JsonProperty("ccdCaseReference")
    Long ccdCaseReference;
}
