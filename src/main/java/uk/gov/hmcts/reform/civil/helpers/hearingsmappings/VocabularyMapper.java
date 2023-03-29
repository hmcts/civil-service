package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.hearingvalues.VocabularyModel;

import java.util.List;

public class VocabularyMapper {

    private VocabularyMapper() {
        //NO-OP
    }

    public static List<VocabularyModel> getVocabulary() {
        return List.of(VocabularyModel.builder().build());
    }
}
