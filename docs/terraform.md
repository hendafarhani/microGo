# Terraform on HCP Terraform (dev)

The `dev` environment runs **plan** and **apply** on [HCP Terraform](https://app.terraform.io)
using API-driven remote execution. The GitHub runner never produces a local plan file — it
only uploads the configuration and triggers a remote run.

- `terraform/environments/dev/providers.tf` declares a `cloud {}` block. The
  organization is not written in code — it is read from the `TF_CLOUD_ORGANIZATION`
  environment variable at runtime.
- `.github/workflows/terraform.yml` has three jobs:
  - **fmt** — `terraform fmt -check -recursive` on the runner (no token needed).
  - **plan** — speculative remote plan on pull requests, or via `workflow_dispatch`
    with `action = plan`.
  - **apply** — `workflow_dispatch` with `action = apply` on `main`, gated by the
    `dev` GitHub Environment. Apply only runs when the remote run is confirmable.

## One-time manual setup (human only)

These steps are intentionally **not** automated. No **tokens** are hardcoded in this
repo — they live in HCP Terraform workspace variables and GitHub secrets. The **org
name** is not in code either — the `cloud {}` block reads it from the
`TF_CLOUD_ORGANIZATION` environment variable (set in CI from the GitHub variable, or
exported locally). The only committed literal is the **workspace name** (`microgo-dev`),
because the `cloud {}` block can't use `var.*` interpolation for it.

1. **Create the HCP Terraform org and workspace.**
   - Create (or reuse) an organization in HCP Terraform.
   - Create a workspace named `microgo-dev`.
   - Set its workflow to **API-driven** and execution mode to **Remote**.
   - Set **Terraform Working Directory** to `environments/dev`. The workflow uploads
     the whole `terraform/` tree (so shared `modules/` are included), and this setting
     makes HCP run from the dev environment inside it. Leaving it blank or set to
     `terraform` causes `No Terraform configuration files found in working directory`.

2. **Add the DigitalOcean token as a workspace variable.**
   - In the `microgo-dev` workspace, add a **Terraform variable** (not env var)
     named `do_token`, mark it **Sensitive**, and paste your DigitalOcean API token.

3. **Configure GitHub.**
   - Repository **secret** `TF_API_TOKEN` — an HCP Terraform user/team API token.
   - Repository **variable** `TF_CLOUD_ORGANIZATION` — your HCP org name. The workflow
     exports it as an environment variable, and the `cloud {}` block reads it to select
     the organization; nothing needs to be changed in `providers.tf`.
   - Create a `dev` **Environment** in GitHub (optionally with required reviewers) to
     gate the apply job.

4. **Local runs (optional).**
   - The org name is not stored in code, so before running Terraform locally, export it:
     `export TF_CLOUD_ORGANIZATION=<your-org>` (matching the GitHub variable).

5. **Migrate state into HCP (one time, locally).**
   - From `terraform/environments/dev`, run a one-time local `terraform init`. Terraform
     detects the new `cloud {}` block and prompts to migrate existing state into the
     `microgo-dev` workspace. Confirm the migration.
   - After migration, all plans/applies run remotely on HCP and this environment no
     longer relies on any previously configured state backend. If you were tracking
     state elsewhere (e.g. a local `terraform.tfstate` or a separate remote backend),
     it is no longer used and can be retired once you've confirmed the HCP workspace
     holds the current state.
