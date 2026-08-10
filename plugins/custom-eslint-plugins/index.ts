import { noDuplicateClassNames } from './no-duplicate-class-names/no-duplicate-class-names';
import { preferStepDecorator } from './prefer-step-decorator/prefer-step-decorator';

const plugins = {
  rules: {
    'no-duplicate-class-names': noDuplicateClassNames,
    'prefer-step-decorator': preferStepDecorator,
  },
};

export = plugins;
