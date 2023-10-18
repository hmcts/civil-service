package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class CaseNoteService {

    private final IdamClient idamClient;
    private final Time time;

    public CaseNote buildCaseNote(String authorisation, String note) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);

        return CaseNote.builder()
            .createdBy(userDetails.getFullName())
            .createdOn(time.now())
            .note(note)
            .build();
    }

    public List<Element<CaseNote>> addNoteToListStart(CaseNote caseNote, List<Element<CaseNote>> caseNotes) {
        List<Element<CaseNote>> updatedCaseNotes = ofNullable(caseNotes).orElse(newArrayList());
        updatedCaseNotes.add(0, element(caseNote));

        return updatedCaseNotes;
    }

    public List<Element<CaseNote>> addNoteToListEnd(CaseNote caseNote, List<Element<CaseNote>> caseNotes) {
        List<Element<CaseNote>> updatedCaseNotes = ofNullable(caseNotes).orElse(newArrayList());
        updatedCaseNotes.add(element(caseNote));

        return updatedCaseNotes;
    }
}
