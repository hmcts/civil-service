terraform {
  backend "azurerm" {}
}

provider "azurerm" {
  alias           = "send-grid"
  subscription_id = var.send_grid_subscription
  features {}
}

provider "azurerm" {
  features {}
}

provider "azuread" {
}