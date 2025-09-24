import DecoratorError from '../errors/decorator-error';

export default class DecoratorHelper {
  private static methodNameToMethodParams = {};

  static verifyParamNames = (
    className: string,
    methodName: string,
    decoratorName: string,
    actualParamNames: string[],
    paramNamesToCheck: string[],
  ) => {
    for (const paramName of paramNamesToCheck) {
      if (!actualParamNames.includes(paramName)) {
        throw new DecoratorError(
          `To use decorator: @${decoratorName}, ${paramName} must be a required parameter on ${className}.${methodName}`,
        );
      }
    }
  };

  private static getParamsStringFromMethodString(originalMethodString: string) {
    const argsMatch = originalMethodString.match(/\(([^)]*)\)/);
    return argsMatch[1].trim();
  }

  private static splitParamsString(paramsString: string): string[] {
    return paramsString.split(',').map((str) => str.replace(/[\n\t\s]+/g, ''));
  }

  private static removeParamsInCurlyBraces(paramsString: string) {
    let result = '';
    let curlyBrace = 0;

    for (let i = 0; i < paramsString.length; i++) {
      if (paramsString[i] === '{') {
        curlyBrace++;
      }
      if (curlyBrace <= 0) {
        result += paramsString[i];
      }
      if (paramsString[i] === '}') {
        curlyBrace--;
      }
    }

    return result;
  }

  private static cleanMethodParams(paramsString: string): string[] {
    const cleanParamsString = this.removeParamsInCurlyBraces(paramsString);
    const cleanParamsList = this.splitParamsString(cleanParamsString).filter(
      (part) => !part.includes('=') && !part.includes('options') && part.length !== 0,
    );
    return cleanParamsList;
  }

  static getParamNamesFromMethod = (
    className: string,
    methodName: string,
    decoratorName: string,
    target: Function,
  ) => {
    let cleanParamList: string[] = this.methodNameToMethodParams[className]?.[methodName];
    if (!cleanParamList) {
      const paramsString = this.getParamsStringFromMethodString(target.toString());
      const paramsList = this.splitParamsString(paramsString);
      cleanParamList = this.cleanMethodParams(paramsString);

      for (let i = 0; i < cleanParamList.length; i++) {
        if (cleanParamList[i] !== paramsList[i]) {
          throw new DecoratorError(
            `To use decorator: @${decoratorName}, ${cleanParamList[i]} must be defined before any default or destructured parameters on method: ${className}.${methodName}`,
          );
        }
      }

      if (!this.methodNameToMethodParams[className]) {
        this.methodNameToMethodParams[className] = {};
      }

      this.methodNameToMethodParams[className][methodName] = cleanParamList;
    }
    return cleanParamList;
  };

  static formatArg = (arg: any) => {
    if (typeof arg === 'string') {
      return `'${arg}'`;
    } else if (Array.isArray(arg)) {
      return `[${arg.join(', ')}]`;
    }
    return arg;
  };

  static formatArgsList = (args: string[]) => {
    return args
      .map((item) => {
        if (typeof item === 'object' && !Array.isArray(item)) {
          return Object.values(item);
        } else {
          return [item];
        }
      })
      .flat();
  };
}
