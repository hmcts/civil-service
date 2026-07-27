import { isAsyncFunction } from 'util/types';
import DecoratorHelper from '../helpers/decorator-helper';
import ClassMethodHelper from '../helpers/class-method-helper';

const checkTruthy = (paramName: string, argValue: any, falsyParams: string[]) => {
  if (!argValue) {
    falsyParams.push(`${paramName}: ${DecoratorHelper.formatArg(argValue)}`);
  }
};

const filterAndValidate = (
  paramNamesToCheck: string[],
  methodParamNames: string[],
  argsValues: any[],
  methodName: string,
  className: string,
) => {
  const falsyParams: string[] = [];
  if (paramNamesToCheck.length === 0) {
    for (const [index, methodParamName] of methodParamNames.entries()) {
      checkTruthy(methodParamName, argsValues[index], falsyParams);
    }
  } else {
    for (const paramName of paramNamesToCheck) {
      const index = methodParamNames.indexOf(paramName);
      checkTruthy(paramName, argsValues[index], falsyParams);
    }
  }
  if (falsyParams.length > 0) {
    className = ClassMethodHelper.formatClassName(className);
    throw new TypeError(
      `Params: (${falsyParams.join(', ')}) on '${className}.${methodName}' method are not truthy`,
    );
  }
};

export const TruthyParams = (classKey: string, ...paramNamesToCheck: string[]) => {
  return function (target: Function, context: ClassMethodDecoratorContext) {
    const methodName = context.name as string;
    const className = ClassMethodHelper.formatClassName(classKey);

    const methodParamNames = DecoratorHelper.getParamNamesFromMethod(
      className,
      methodName,
      TruthyParams.name,
      target,
    );
    DecoratorHelper.verifyParamNames(
      className,
      methodName,
      TruthyParams.name,
      methodParamNames,
      paramNamesToCheck,
    );

    if (isAsyncFunction(target)) {
      return async function asyncReplacementMethod(this: any, ...args: any[]) {
        filterAndValidate(paramNamesToCheck, methodParamNames, args, methodName, className);
        return await target.call(this, ...args);
      };
    }

    return function replacementMethod(this: any, ...args: any[]) {
      filterAndValidate(paramNamesToCheck, methodParamNames, args, methodName, className);
      return target.call(this, ...args);
    };
  };
};
