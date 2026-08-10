'use strict';
Object.defineProperty(exports, '__esModule', { value: true });
exports.noDuplicateClassNames = void 0;
const utils_1 = require('@typescript-eslint/utils');
const classNames = {};
const createRule = utils_1.ESLintUtils.RuleCreator((name) => `https://example.com/rule/${name}`);
exports.noDuplicateClassNames = createRule({
  name: 'no-duplicate-class-names',
  meta: {
    docs: {
      recommended: 'error',
      description: 'Enforces all class definitions to have unique names',
    },
    messages: {
      default: "'{{callee}}' has been duplicated",
    },
    type: 'problem',
    schema: [
      {
        type: 'object',
        properties: {
          functionNames: {
            type: 'array',
            items: [
              {
                type: 'string',
              },
            ],
            uniqueItems: true,
          },
        },
      },
    ],
  },
  defaultOptions: [],
  create(context) {
    return {
      ClassDeclaration(node) {
        if (classNames[node.id.name] && classNames[node.id.name] !== context.getFilename()) {
          context.report({
            node,
            data: { callee: node.id.name },
            messageId: 'default',
          });
        } else {
          classNames[node.id.name] = context.getFilename();
        }
      },
    };
  },
});
