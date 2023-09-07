package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDetailsForm {

    private DynamicList partyChosen;
    private String partyChosenId;
    private String partyChosenType;
    private YesOrNo hidePartyChoice;
    private List<Element<UnavailableDate>> additionalUnavailableDates;
    private List<Element<UpdatePartyDetailsForm>> updateExpertsDetailsForm;
    private List<Element<UpdatePartyDetailsForm>> updateWitnessesDetailsForm;


    public List<Element<Expert>> getExperts() {
        List<Element<Expert>> newExperts = new ArrayList<>();

        if (updateExpertsDetailsForm.isEmpty()) {
            return Collections.emptyList();
        }

        for (Element<UpdatePartyDetailsForm> party : updateExpertsDetailsForm) {
            UpdatePartyDetailsForm expert = party.getValue();
            newExperts.addAll(wrapElements(Expert.builder()
                                               .firstName(expert.getFirstName())
                                               .lastName(expert.getLastName())
                                               .emailAddress(expert.getEmailAddress())
                                               .phoneNumber(expert.getPhoneNumber())
                                               .fieldOfExpertise(expert.getFieldOfExpertise())
                                               .build()));
        }

        return newExperts;
    }

    public void setExperts(List<Element<Expert>> experts) {
        this.updateExpertsDetailsForm = new ArrayList<>();

        if (!updateExpertsDetailsForm.isEmpty()) {
            for (Element<Expert> party : experts) {
                Expert expert = party.getValue();
                this.updateExpertsDetailsForm.addAll(wrapElements(UpdatePartyDetailsForm.builder()
                                                                      .firstName(expert.getFirstName())
                                                                      .lastName(expert.getLastName())
                                                                      .emailAddress(expert.getEmailAddress())
                                                                      .phoneNumber(expert.getPhoneNumber())
                                                                      .fieldOfExpertise(expert.getFieldOfExpertise())
                                                                      .build()));
            }
        }
    }
}

