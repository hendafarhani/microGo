variable "do_token" {
  type      = string
  sensitive = true
}

variable "project_name" {
  type    = string
  default = "microgo-dev"
}

variable "region" {
  type    = string
  default = "fra1"
}

variable "k8s_version" {
  type    = string
  default = "1.34.5-do.1"
}

variable "node_size" {
  type    = string
  default = "s-2vcpu-2gb"
}

variable "node_count" {
  type    = number
  default = 2
}

variable "registry_name" {
  type    = string
  default = "microgo-registry"
}

variable "namespace_name" {
  type    = string
  default = "microgo-dev"
}

variable "domain_name" {
  type    = string
  default = "hendafarhani.cloud"
}

variable "subdomain" {
  type    = string
  default = "dev-microgo"
}

variable "create_dns_record" {
  type    = bool
  default = false
}

variable "dns_record_value" {
  type    = string
  default = ""
}
