import BaseSchemaBuilderFactory from '../../../base/base-schema-builder-factory';
import CreateSdoSchemaBuilder from './create-sdo/create-sdo-schema-builder';
import GenerateDirectionsOrderSchemaBuilder from './generate-directions-order/generate-directions-order-schema-builder';
import NotSuitableSdoSchemaBuilder from './not-suitable-sdo/not-suitable-sdo-schema-builder';

export default class JudgeLASchemaBuilderFactory extends BaseSchemaBuilderFactory {
  get createSdoSchemaBuilder() {
    return new CreateSdoSchemaBuilder(this.testData);
  }

  get generateDirectionsOrderSchemaBuilder() {
    return new GenerateDirectionsOrderSchemaBuilder(this.testData);
  }

  get notSuitableSdoSchemaBuilder() {
    return new NotSuitableSdoSchemaBuilder(this.testData);
  }
}
