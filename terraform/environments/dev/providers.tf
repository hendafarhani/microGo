terraform {
  required_version = ">= 1.5.0"

  cloud {
    # organization is supplied by the TF_CLOUD_ORGANIZATION environment variable so it
    # is not hardcoded here. The GitHub Actions workflow sets it from
    # vars.TF_CLOUD_ORGANIZATION; for local runs, export TF_CLOUD_ORGANIZATION=<org>.
    # (The cloud block can't use normal var.* interpolation, but it does read this
    # specific env var.)
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
