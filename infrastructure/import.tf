import {
  for_each = var.env == "prod" ? toset(["import"]) : toset([])
  to       = module.key-vault.azurerm_key_vault_access_policy.creator_access_policy[0]
  id       = "/subscriptions/8999dec3-0104-4a27-94ee-6588559729d1/resourceGroups/civil-service-prod/providers/Microsoft.KeyVault/vaults/civil-prod/objectId/c860eaa0-74be-4731-8370-db94c5fdad81"
}
