package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.caseProgression.UploadEvidenceExpert4;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class PartyNameService {

    private final IdamClient idamClient;

    public UploadEvidenceExpert4 buildPartyName(String authorisation, String partyName) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        return UploadEvidenceExpert4.builder()
            .expertOption4OtherName(partyName)
            .build();
    }

    public List<Element<UploadEvidenceExpert4>> addPartyToTextField(UploadEvidenceExpert4 partyNote,
                                                                    List<Element<UploadEvidenceExpert4>> partyNotes) {
        List<Element<UploadEvidenceExpert4>> updatedPartyName = ofNullable(partyNotes).orElse(newArrayList());
        updatedPartyName.add(0, element(partyNote));

        return updatedPartyName;
    }
}
