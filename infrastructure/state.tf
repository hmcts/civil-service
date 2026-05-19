terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.70.0"
    }
    random = {
      source = "hashicorp/random"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.53.1"
    }
  }
}

provider "azurerm" {
  alias           = "send-grid"
  subscription_id = var.send_grid_subscription
  features {}
}

provider "azurerm" {
  alias                           = "cnp_dev"
  subscription_id                 = "1c4f0704-a29e-403d-b719-b90c34ef14c9"
  resource_provider_registrations = "none"
  features {}
}

provider "azurerm" {
  features {}
}

provider "azuread" {
}
