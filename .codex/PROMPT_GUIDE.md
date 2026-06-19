# microGo Prompt Guide

Use this guide to get the most value from the repository agent system:

- `AGENTS.md`
- `.codex/agents/`
- `.codex/memories/`
- `.codex/workflows/change-orchestration.md`
- `.codex/workflows/session-prompts.md`

This file is intentionally practical. Copy a prompt, replace the placeholders, and run it.

## Start Every Session With This Rule

Before asking for implementation work, tell the agent to load the repository context first.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/current-focus.md
- .codex/memories/testing-and-ops.md
- .codex/workflows/change-orchestration.md

Then help me with this task:
{{TASK}}
```

## Most Used Prompts

## 1. Full Multi-Agent Delivery

Use this for any non-trivial feature, refactor, bug fix, infra change, or cross-service change.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/current-focus.md
- .codex/memories/testing-and-ops.md
- .codex/workflows/change-orchestration.md

Use the right agents from `.codex/agents/` and orchestrate the work in this order:
1. Architecture
2. Implementation
3. QA
4. Review
5. Docs and memory update if needed

Task:
{{TASK}}

Expected output:
- architecture note
- implementation summary
- tests run or not run
- review findings or explicit no-findings result
- memory/doc updates made
```

## 2. Architecture First

Use this when you want design clarity before code changes.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/current-focus.md
- .codex/agents/architecture-agent.md

Task:
{{TASK}}

Before any code changes, give me:
1. which service or infra area should own the change
2. event and data flow impact
3. risks and tradeoffs
4. implementation constraints
5. recommended test strategy
```

## 3. Implement After Architecture

Use this when the design is already clear and you want focused execution.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/agents/implementation-agent.md

Task:
{{TASK}}

Architecture note:
{{ARCHITECTURE_NOTE}}

Implement the smallest complete change in the correct service.
Then summarize:
- files changed
- behavior changed
- assumptions made
- tests added or still needed
```

## 4. QA Pass

Use this after implementation to improve confidence.

```text
Read and follow:
- AGENTS.md
- .codex/memories/testing-and-ops.md
- .codex/agents/qa-agent.md

Task:
{{TASK}}

Implementation summary:
{{IMPLEMENTATION_SUMMARY}}

Add or update the right tests and tell me:
1. what you tested
2. what commands you ran
3. what remains unverified
```

## 5. Review Pass

Use this when you want a bug-focused code review.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/agents/review-agent.md

Task:
{{TASK}}

Review the resulting changes for:
- bugs
- regressions
- weak tests
- maintainability issues
- missing docs or memory updates

Return findings first, ordered by severity. If there are no findings, say so explicitly.
```

## 6. Platform Or Infra Change

Use this for Docker, Helm, Terraform, config server, CI, or deployment-related work.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/testing-and-ops.md
- .codex/agents/platform-agent.md

Task:
{{TASK}}

Check impact across:
- docker-compose
- centralized config
- helm
- terraform
- github workflows

Then implement the change and tell me what must stay aligned.
```

## 7. Bug Fix With Full Discipline

Use this for production-style debugging.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/testing-and-ops.md
- .codex/workflows/change-orchestration.md

Task:
{{BUG_DESCRIPTION}}

Approach:
1. identify the likely owning service
2. explain the root cause
3. implement the smallest safe fix
4. add or update tests
5. review for regressions
6. update docs or memory if future debugging should benefit
```

## 8. Cross-Service Feature

Use this when a feature touches more than one service or both app and infra.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/current-focus.md
- .codex/memories/testing-and-ops.md
- .codex/workflows/change-orchestration.md

Task:
{{FEATURE_DESCRIPTION}}

Treat this as a cross-service change.
Start with the Architecture Agent.
Then orchestrate Implementation, QA, Review, and any Platform work needed.

Be explicit about:
- service ownership
- contracts that change
- deployment/config changes
- test coverage by service
```

## 9. Docs And Memory Refresh

Use this after important work so the system stays useful.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/current-focus.md
- .codex/memories/testing-and-ops.md

Task:
{{CHANGE_SUMMARY}}

Update only the docs and memory files that should change because of this work.
Keep the memory concise and execution-oriented.
Tell me what you updated and why.
```

## 10. Session Continuation

Use this when you return later and want continuity without re-explaining everything.

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/current-focus.md
- .codex/memories/testing-and-ops.md

Continue working on microGo from the existing repository state.

Current objective:
{{OBJECTIVE}}

Before changing code:
1. summarize the current repo state relevant to this objective
2. identify the likely next step
3. execute it using the right agent workflow
```

## 11. Strict Minimal Change

Use this when you want the agent to stay narrow and avoid opportunistic refactors.

```text
Read and follow:
- AGENTS.md
- .codex/agents/implementation-agent.md
- .codex/agents/review-agent.md

Task:
{{TASK}}

Constraints:
- make the smallest complete change
- do not refactor unrelated code
- do not widen scope unless required for correctness
- explain any scope expansion before doing it
```

## 12. Local Validation Support

Use this when you want help verifying behavior with the existing local stack.

```text
Read and follow:
- AGENTS.md
- .codex/memories/testing-and-ops.md
- LOCAL_TESTING.md

Task:
{{TASK}}

Use the existing local testing workflow where applicable.
Tell me:
1. what validation path fits this change
2. what commands should be run
3. what success looks like
4. what evidence would indicate a regression
```

## Recommended Prompting Pattern

When you want the best results, combine:

1. the task
2. the target service or area if you know it
3. the expected depth
4. whether you want architecture first or direct implementation
5. whether docs and memory should be updated

Example:

```text
Read and follow:
- AGENTS.md
- .codex/memories/project-context.md
- .codex/memories/current-focus.md
- .codex/memories/testing-and-ops.md
- .codex/workflows/change-orchestration.md

Task:
Add retry-safe acknowledgement handling for outbox events in outbox-publisher-service and any required supporting changes in ride-request.

I want:
- architecture first
- implementation after approval of the approach
- tests updated
- review findings
- memory/docs updated if the operational workflow changes
```

## Which File Does What

- `AGENTS.md`: top-level operating contract for this repo
- `.codex/agents/`: specialist behavior by role
- `.codex/memories/`: durable repo context and current priorities
- `.codex/workflows/change-orchestration.md`: execution order across agents
- `.codex/workflows/session-prompts.md`: role-specific prompt templates

## Maintenance Rule

If a prompt pattern becomes common, add it here instead of rewriting it every session.
