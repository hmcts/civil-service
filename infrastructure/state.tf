terraform {
  backend "azurerm" {}
}

provider "azurerm" {
  alias = "send-grid"
  features {}
}
