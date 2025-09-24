'use strict';
Object.defineProperty(exports, '__esModule', { value: true });
exports.preferStepDecorator = void 0;
const utils_1 = require('@typescript-eslint/utils');
const createRule = utils_1.ESLintUtils.RuleCreator((name) => `https://example.com/rule/${name}`);
exports.preferStepDecorator = createRule({
  name: 'prefer-step-decorator',
  meta: {
    docs: {
      recommended: 'warn',
      description:
        'Prefers all class to have AllMethodStep decorator or at least one class method to have a Step decorator',
    },
    messages: {
      default:
        "'{{callee}}' does not have class level AllMethodStep or method level Step decorator",
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
        let hasStepDecorator = false;
        if (node.decorators) {
          for (const decorator of node.decorators) {
            const decoratorExpression = decorator.expression;
            if (decoratorExpression.callee.name === 'AllMethodsStep') {
              hasStepDecorator = true;
              break;
            }
          }
        }
        if (node.body.body) {
          for (const method of node.body.body) {
            if ('decorators' in method) {
              for (const decorator of method.decorators || []) {
                const decoratorExpression = decorator.expression;
                if (
                  decoratorExpression.callee.name === 'BoxedDetailedStep' ||
                  decoratorExpression.callee.name === 'Step'
                ) {
                  hasStepDecorator = true;
                  break;
                }
              }
            }
          }
        }
        if (!hasStepDecorator) {
          context.report({
            node,
            data: { callee: node.id.name },
            messageId: 'default',
          });
        }
      },
    };
  },
});
