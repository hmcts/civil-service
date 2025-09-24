export default class ObjectHelper {
  /**
   * {...obj1, ...obj2} replaces elements. For instance, if obj1 = { check : { correct : false }}
   * and obj2 = { check: { newValue : 'ASDF' }} the result will be { check : {newValue : 'ASDF} }.
   *
   * What this method does is a kind of deep spread, in a case like the one before,
   * @param objectToBeModified the object we want to modify
   * @param modificationObject the object holding the modifications
   * @return a data object with the new values
   */
  static deepSpread(objectToBeModified: object, modificationObject: object) {
    const modified = { ...objectToBeModified };
    for (const key in modificationObject) {
      if (objectToBeModified[key] && typeof objectToBeModified[key] === 'object') {
        if (Array.isArray(objectToBeModified[key])) {
          modified[key] = modificationObject[key];
        } else {
          modified[key] = this.deepSpread(objectToBeModified[key], modificationObject[key]);
        }
      } else {
        modified[key] = modificationObject[key];
      }
    }
    return modified;
  }
}
