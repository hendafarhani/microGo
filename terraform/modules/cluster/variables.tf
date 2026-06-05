variable "name" {
  type = string
}

variable "region" {
  type = string
}

variable "k8s_version" {
  type = string
}

variable "node_size" {
  type = string
}

variable "node_count" {
  type = number
}

variable "tags" {
  type    = list(string)
  default = []
}
