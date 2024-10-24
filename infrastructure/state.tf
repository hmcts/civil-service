terraform {
  backend "azurerm" {}

  required_providers {
      azurerm = {
        source  = "hashicorp/azurerm"
        version = "4.0.1"
      }
      random = {
        source = "hashicorp/random"
      }
      azuread = {
        source  = "hashicorp/azuread"
        version = "3.0.2"
      }
    }
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
