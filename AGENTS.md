# microGo Agent Operating Guide

This repository is set up for multi-agent work. Use the files under `.codex/` as the source of truth for how to plan, implement, test, review, and document changes.

## Primary Objective

Build and evolve `microGo` as a production-style ride-sharing backend with:

- Spring Boot microservices
- Kafka and RabbitMQ event flows
- Redis and MySQL persistence
- Docker Compose for local integration
- Helm and Terraform for Kubernetes-oriented deployment

## Repository Reality

- The root repository coordinates multiple services and infrastructure folders.
- Some service directories are maintained as submodules or independently versioned units.
- Changes may span app code, config, Docker, Helm, Terraform, and docs.
- Current local modifications already exist in `ride-request` and `location-saver`; do not overwrite unrelated work.

## Required Working Pattern

For non-trivial work, follow this sequence:

1. Read `.codex/memories/project-context.md`.
2. Read `.codex/memories/current-focus.md`.
3. Pick the relevant specialist instructions from `.codex/agents/`.
4. Follow `.codex/workflows/change-orchestration.md`.
5. After finishing, update memory files if the architecture, priorities, or runbooks changed.

## Specialist Agents

- Architecture: `.codex/agents/architecture-agent.md`
- Implementation: `.codex/agents/implementation-agent.md`
- QA: `.codex/agents/qa-agent.md`
- Review: `.codex/agents/review-agent.md`
- Platform: `.codex/agents/platform-agent.md`

## Memory Discipline

Update memory files when any of these change:

- service boundaries or event contracts
- local testing steps
- infrastructure rollout shape
- active priorities or unresolved risks
- important repo-specific conventions

Keep memory files short, current, and biased toward future execution rather than historical narration.

## Definition Of Done

A change is not complete until it includes:

- the correct service or infra target
- tests or an explicit explanation for why tests were not added
- documentation updates when behavior or workflow changed
- memory updates when the change affects future agent decisions

## Default Collaboration Style

- Prefer focused changes over broad refactors.
- Preserve service boundaries.
- Treat cross-service changes as architecture work first, coding second.
- Call out assumptions clearly when a requirement is ambiguous.
