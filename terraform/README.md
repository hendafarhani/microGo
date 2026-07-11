# Terraform Infrastructure for microGo

This folder defines the cloud infrastructure for the `microGo` platform using Terraform.

The goal is to provision and manage the deployment foundation (Kubernetes, registry, DNS) in a repeatable and versioned way.

## What this Terraform project manages

- `modules/cluster`: creates a DigitalOcean Kubernetes cluster (DOKS).
- `modules/registry`: creates a DigitalOcean Container Registry for microservice images.
- `modules/namespace`: creates a real Kubernetes namespace using the Kubernetes provider.
- `modules/dns`: manages DNS records for exposing services through a domain/subdomain.
- `environments/dev`: composes all modules for the `dev` environment.

## Project structure

- `modules/`: reusable building blocks.
- `environments/`: environment-specific assembly (variables, module wiring, state scope).

State for the `dev` environment is managed remotely by **HCP Terraform** (Terraform
Cloud), not by a backend config file in this repo. See [../docs/terraform.md](../docs/terraform.md)
for the full HCP Terraform setup and one-time state migration procedure.

## Typical workflow

1. Define or update infrastructure in module/environment code.
2. Validate and preview changes (`terraform validate`, `terraform plan`).
3. Apply changes (`terraform apply`).
4. Deploy application workloads (Helm/manifests) into the created cluster/namespace.

## GitHub Actions

The repository includes `.github/workflows/terraform.yml` for the `dev` environment. It
uses HCP Terraform's API-driven remote execution — the runner uploads the configuration
and triggers a remote run, so no local plan file is produced:

- **fmt** — runs `terraform fmt -check -recursive` on the runner (no secrets needed).
- **plan** — a speculative remote plan on pull requests from branches in this repository,
  or via `workflow_dispatch` with `action = plan`. Forked PRs are skipped because they
  don't receive repository secrets.
- **apply** — `workflow_dispatch` with `action = apply` from `main`, gated by the `dev`
  GitHub Environment. It only applies when the remote run is confirmable and fails loudly
  otherwise.

Configure these before running the workflow:

- Repository **secret** `TF_API_TOKEN` — an HCP Terraform user/team API token.
- Repository **variable** `TF_CLOUD_ORGANIZATION` — your HCP org name (must match the
  `organization` in `environments/dev/providers.tf`).
- HCP workspace **variable** `do_token` (Sensitive) — your DigitalOcean API token, set in
  the `microgo-dev` workspace.
- A `dev` GitHub **Environment** (optionally with required reviewers) to gate apply.

The workflow does not upload binary Terraform plan files as artifacts because they can
contain sensitive values.

For a public repository, configure these GitHub settings:

- Protect `main`: require a pull request before merging, require at least one approval, and block direct pushes.
- Protect the `dev` environment: add required reviewers and restrict deployment branches to `main`.
- Keep Terraform apply manual only; do not add automatic apply on push.

## Quick start (dev)

Day-to-day changes go through the GitHub Actions workflow above. To run locally against
HCP Terraform, from `terraform/environments/dev`:

```bash
export TF_API_TOKEN="your_hcp_terraform_api_token"
terraform init      # detects the cloud {} block and connects to the microgo-dev workspace
terraform fmt -recursive
terraform validate
terraform plan       # runs remotely on HCP
```

To apply:

```bash
terraform apply      # runs remotely on HCP; confirm the run when prompted
```

The `do_token` variable is read from the HCP workspace, so it does not need to be set
locally. First-time setup and state migration are covered in [../docs/terraform.md](../docs/terraform.md).

## Notes

- `create_dns_record` is `false` by default because you may not have a public LB/Ingress IP yet.
- Set `create_dns_record=true` and provide `dns_record_value` after your public endpoint exists.
- Keep sensitive values out of Git (use HCP workspace variables, secret stores, or CI secrets).
- Use a separate HCP workspace per environment (`dev`, `staging`, `prod`).
