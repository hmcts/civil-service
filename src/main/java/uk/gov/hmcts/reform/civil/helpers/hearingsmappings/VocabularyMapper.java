package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.hearings.hearingrequest.model.VocabularyModel;

import java.util.List;

public class VocabularyMapper {

    private VocabularyMapper() {
        //NO-OP
    }

    public static List<VocabularyModel> getVocabulary() {
        return List.of(VocabularyModel.builder().build());
    }
}
