import { isAsyncFunction } from 'util/types';
import DecoratorError from '../errors/decorator-error';
import DecoratorHelper from '../helpers/decorator-helper';
import { test } from '../playwright-fixtures/index';
import ClassMethodHelper from '../helpers/class-method-helper';

//DO NOT APPLY AllMethodStep Decorator with Step decorator

const stepFlag = '__allMethodsStepApplied';

const verifyMethodNamesToIgnore = (
  methodsNamesToIgnore: string[],
  classMethodNames: string[],
  className: string,
) => {
  for (const methodName of methodsNamesToIgnore) {
    if (methodName === stepFlag) {
      throw new DecoratorError(`Invalid method name to ignore: ${methodName}, class: ${className}`);
    }
    if (!classMethodNames.includes(methodName)) {
      throw new DecoratorError(`${methodName} is not a method name in class: ${className}`);
    }
  }
};

const getStepDetailed = (
  paramNamesToDetail: string[],
  methodParamNames: string[],
  argsValues: any[],
  className: string,
  methodName: string,
) => {
  const detailedParams: string[] = [];

  for (const [index, methodParamName] of methodParamNames.entries()) {
    if (paramNamesToDetail.includes(methodParamName)) {
      detailedParams.push(`${methodParamName}: ${DecoratorHelper.formatArg(argsValues[index])}`);
    }
  }

  return `${className}.${methodName}${paramNamesToDetail.length === 0 ? '' : `(${detailedParams.join(', ')})`}`;
};

export const Step = function (classKey: string) {
  return function (target: Function, context: ClassMethodDecoratorContext) {
    const methodName = context.name as string;
    const className = ClassMethodHelper.formatClassName(classKey);
    if (target.prototype && target.prototype[stepFlag]) {
      throw new DecoratorError(
        `${Step.name} decorator cannot be applied when @${AllMethodsStep.name} decorator is already applied.`,
      );
    }
    if (!isAsyncFunction(target)) {
      throw new DecoratorError(
        `${className}.${methodName} must be asynchronous to use @${Step.name} decorator`,
      );
    }

    return async function replacementMethod(this: any, ...args: any) {
      const stepName = `${className}.${methodName}`;
      return await test.step(stepName, async () => {
        return await target.call(this, ...args);
      });
    };
  };
};

export const BoxedDetailedStep = function (classKey: string, ...paramNamesToDetail: string[]) {
  return function (target: Function, context: ClassMethodDecoratorContext) {
    const methodName = context.name as string;
    const className = ClassMethodHelper.formatClassName(classKey);

    if (target.prototype && target.prototype[stepFlag]) {
      throw new DecoratorError(
        `${Step.name} decorator cannot be applied when @${AllMethodsStep.name} decorator is already applied.`,
      );
    }
    if (!isAsyncFunction(target)) {
      throw new DecoratorError(
        `${className}.${methodName} must be asynchronous to use @${Step.name} decorator`,
      );
    }

    const methodParamNames = DecoratorHelper.getParamNamesFromMethod(
      className,
      methodName,
      BoxedDetailedStep.name,
      target,
    );

    DecoratorHelper.verifyParamNames(
      className,
      methodName,
      BoxedDetailedStep.name,
      methodParamNames,
      paramNamesToDetail,
    );

    return async function replacementMethod(this: any, ...args: any[]) {
      const formattedArgs = DecoratorHelper.formatArgsList(args);
      const stepName = getStepDetailed(
        paramNamesToDetail,
        methodParamNames,
        formattedArgs,
        className,
        methodName,
      );
      return await test.step(
        stepName,
        async () => {
          return await target.call(this, ...args);
        },
        { box: true },
      );
    };
  };
};

export const AllMethodsStep = ({ methodNamesToIgnore = [] as string[] } = {}) => {
  return function (target: Function, context: ClassDecoratorContext) {
    const targetClass = target;
    const classMethodNames = Object.getOwnPropertyNames(targetClass.prototype);

    verifyMethodNamesToIgnore(methodNamesToIgnore, classMethodNames, targetClass.name);

    for (const methodName of classMethodNames) {
      const method = targetClass.prototype[methodName];
      if (
        typeof method === 'function' &&
        methodName !== 'constructor' &&
        !methodNamesToIgnore.includes(methodName)
      ) {
        const stepName = ClassMethodHelper.formatClassName(targetClass.name) + '.' + methodName;
        if (!isAsyncFunction(method)) {
          throw new DecoratorError(
            `All methods defined in ${targetClass.name} must be asynchronous to use @${AllMethodsStep.name} decorator`,
          );
        }
        targetClass.prototype[methodName] = async function (...args: any[]) {
          return await test.step(stepName, async () => {
            return await method.apply(this, args);
          });
        };
      }
    }
  };
};
