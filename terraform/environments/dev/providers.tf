terraform {
  required_version = ">= 1.5.0"

  cloud {
    organization = "YOUR_HCP_ORG"

    workspaces {
      name = "microgo-dev"
    }
  }

  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = "~> 2.40"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.32"
    }
  }
}

provider "digitalocean" {
  token = var.do_token
}

provider "kubernetes" {
  host                   = local.k8s_host
  token                  = local.k8s_token
  cluster_ca_certificate = local.k8s_ca
}
