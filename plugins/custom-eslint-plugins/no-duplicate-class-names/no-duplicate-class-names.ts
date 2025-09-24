import { ESLintUtils } from '@typescript-eslint/utils';

type MessageIds = 'default';

const classNames: Record<string, string> = {};

type Options = { functionNames: string[] }[];

const createRule = ESLintUtils.RuleCreator((name) => `https://example.com/rule/${name}`);

export const noDuplicateClassNames = createRule<Options, MessageIds>({
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
        if (classNames[node.id!.name] && classNames[node.id!.name] !== context.getFilename()) {
          context.report({
            node,
            data: { callee: node.id!.name },
            messageId: 'default',
          });
        } else {
          classNames[node.id!.name] = context.getFilename();
        }
      },
    };
  },
});
