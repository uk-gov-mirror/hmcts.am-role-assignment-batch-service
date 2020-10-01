locals {
  app_full_name = join("-", [var.product, var.component])
}