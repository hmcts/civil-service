import BaseDataBuilder from '../../../../base/base-data-builder';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import addCaseNoteDataBuilderComponents from './add-case-note-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class AddCaseNoteDataBuilder extends BaseDataBuilder {
  async buildData() {
    return {
      ...addCaseNoteDataBuilderComponents.caseNote,
    };
  }
}
