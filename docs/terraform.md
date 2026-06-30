# Terraform on HCP Terraform (dev)

The `dev` environment runs **plan** and **apply** on [HCP Terraform](https://app.terraform.io)
using API-driven remote execution. The GitHub runner never produces a local plan file — it
only uploads the configuration and triggers a remote run.

- `terraform/environments/dev/providers.tf` declares a `cloud {}` block (replace
  `YOUR_HCP_ORG` with your real organization name).
- `.github/workflows/terraform.yml` has three jobs:
  - **fmt** — `terraform fmt -check -recursive` on the runner (no token needed).
  - **plan** — speculative remote plan on pull requests, or via `workflow_dispatch`
    with `action = plan`.
  - **apply** — `workflow_dispatch` with `action = apply` on `main`, gated by the
    `dev` GitHub Environment. Apply only runs when the remote run is confirmable.

## One-time manual setup (human only)

These steps are intentionally **not** automated. None of the tokens or org/workspace
names are hardcoded in this repo.

1. **Create the HCP Terraform org and workspace.**
   - Create (or reuse) an organization in HCP Terraform.
   - Create a workspace named `microgo-dev`.
   - Set its workflow to **API-driven** and execution mode to **Remote**.

2. **Add the DigitalOcean token as a workspace variable.**
   - In the `microgo-dev` workspace, add a **Terraform variable** (not env var)
     named `do_token`, mark it **Sensitive**, and paste your DigitalOcean API token.

3. **Configure GitHub.**
   - Repository **secret** `TF_API_TOKEN` — an HCP Terraform user/team API token.
   - Repository **variable** `TF_CLOUD_ORGANIZATION` — your HCP org name (must match
     the `organization` in `providers.tf`).
   - Create a `dev` **Environment** in GitHub (optionally with required reviewers) to
     gate the apply job.

4. **Set the org name in code.**
   - Replace the `YOUR_HCP_ORG` placeholder in `providers.tf` with your real org name.

5. **Migrate state into HCP (one time, locally).**
   - From `terraform/environments/dev`, run a one-time local `terraform init`. Terraform
     detects the new `cloud {}` block and prompts to migrate existing state into the
     `microgo-dev` workspace. Confirm the migration.
   - After migration, all plans/applies run remotely on HCP; the old DigitalOcean Spaces
     (S3) backend is no longer used and can be retired.
