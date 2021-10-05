variable "product" {
  type = string
}

variable "raw_product" {
  type    = string
  default = "am"
  // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
  type = string
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "subscription" {
  type = string
}

variable "ilbIp" {
  type = string
}

variable "common_tags" {
  type = map(string)
}