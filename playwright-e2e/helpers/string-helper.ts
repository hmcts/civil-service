export default class StringHelper {
  static capitalise(str: string) {
    const firstChar = str.charAt(0).toUpperCase();
    const restOfStr = str.substring(1);
    return `${firstChar}${restOfStr}`;
  }

  static decapitalise(str: string) {
    let newStr = '';
    let keepDecapitalising = true;
    for (let i = 1; i <= str.length; i++) {
      if (keepDecapitalising) {
        if (i === str.length || (this.isUpper(str[i - 1]) && this.isUpper(str[i]))) {
          newStr += str[i - 1].toLowerCase();
        } else if (this.isUpper(str[i - 1]) && this.isLower(str[i])) {
          keepDecapitalising = false;
        }
      }
      if (!keepDecapitalising) {
        if (i === 1) newStr += str[i - 1].toLowerCase();
        else newStr += str[i - 1];
      }
    }
    return newStr;
  }

  static isUpper(str: string) {
    return str === str.toUpperCase();
  }

  static isLower(str: string) {
    return str === str.toLowerCase();
  }
}
