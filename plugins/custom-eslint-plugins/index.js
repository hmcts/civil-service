'use strict';
const no_duplicate_class_names_1 = require('./no-duplicate-class-names/no-duplicate-class-names');
const prefer_step_decorator_1 = require('./prefer-step-decorator/prefer-step-decorator');
const plugins = {
  rules: {
    'no-duplicate-class-names': no_duplicate_class_names_1.noDuplicateClassNames,
    'prefer-step-decorator': prefer_step_decorator_1.preferStepDecorator,
  },
};
module.exports = plugins;
