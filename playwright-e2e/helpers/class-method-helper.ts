import StringHelper from './string-helper';

export default class ClassMethodHelper {
  static formatClassName = (className: string) => {
    if (className.endsWith('Steps')) {
      return className;
    }
    return StringHelper.decapitalise(className);
  };
}
