# Change Orchestration Workflow

Use this workflow to coordinate multi-agent work for `microGo`.

## Step 1: Load Memory

Read:

- `.codex/memories/project-context.md`
- `.codex/memories/current-focus.md`
- `.codex/memories/testing-and-ops.md`

## Step 2: Triage The Request

Classify the work:

- domain logic change
- event contract change
- persistence change
- local runtime change
- deployment or infrastructure change
- documentation-only change

## Step 3: Select Agents

Use this default mapping:

- Architecture Agent: any cross-service, infra-heavy, or contract-heavy request
- Implementation Agent: any code or config change
- QA Agent: any behavioral change
- Review Agent: all non-trivial changes before handoff
- Platform Agent: Docker, CI, Helm, Terraform, config server, or environment work

## Step 4: Execute In Order

1. Architecture note
2. Implementation
3. QA and verification
4. Review pass
5. Memory and docs update

For small single-service fixes, Architecture can be skipped if service ownership is already obvious.

## Step 5: Handoff Format

Each agent should leave concise output for the next one:

- scope
- changed files
- important assumptions
- verification status
- remaining risks

## Step 6: Close The Loop

Before considering the work complete:

- update docs if user-facing or operational behavior changed
- update memory if future agent decisions should change
- note tests run and tests not run
