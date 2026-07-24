terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.80.0"
    }
    random = {
      source = "hashicorp/random"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.9.0"
    }
  }
}

provider "azurerm" {
  alias                           = "send-grid"
  subscription_id                 = var.send_grid_subscription
  resource_provider_registrations = "none"

  enhanced_validation {
    resource_providers = false
  }

  features {}
}

provider "azurerm" {
  features {}
}

provider "azuread" {
}
