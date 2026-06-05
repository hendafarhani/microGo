variable "domain_name" {
  type = string
}

variable "subdomain" {
  type = string
}

variable "create_record" {
  type    = bool
  default = false
}

variable "record_value" {
  type    = string
  default = ""
}

variable "ttl" {
  type    = number
  default = 60
}
