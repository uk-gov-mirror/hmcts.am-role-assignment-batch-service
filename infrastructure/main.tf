locals {
  app_full_name = join("-", [var.product, var.component])
  
  # local_env = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env

  # // Vault name
  # previewVaultName = "${var.raw_product}-aat"
  # nonPreviewVaultName = "${var.raw_product}-${var.env}"
  # vaultName = (var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName

  # // Shared Resource Group
  # previewResourceGroup = "${var.raw_product}-shared-infrastructure-aat"
  # nonPreviewResourceGroup = "${var.raw_product}-shared-infrastructure-${var.env}"
  # sharedResourceGroup = (var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup

}

# data "azurerm_key_vault" "am_key_vault" {
#   name = local.vaultName
#   resource_group_name = local.sharedResourceGroup
# }

# data "azurerm_key_vault" "s2s_vault" {
#   name = "s2s-${local.local_env}"
#   resource_group_name = "rpe-service-auth-provider-${local.local_env}"
# }