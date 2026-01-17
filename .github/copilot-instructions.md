# Copilot Repository Instructions

## Non-negotiable rules
- DO NOT create documentation files anywhere except:
    - /docs/**
    - /doc-meta/**
- DO NOT create random *.md files in source folders.
- When asked to document something, update existing docs first; only create a new doc if no suitable place exists under /docs/**.
- Prefer references over duplication. If content exists, link to it.

## Documentation sources of truth
- Always treat these as authoritative:
    - /doc-meta/system.yaml
    - /doc-meta/glossary.yaml
    - /docs/04_decisions/** (ADRs)

## How to work while coding
- Before implementing or changing behavior, check relevant docs:
    - architecture: /docs/01_architecture/**
    - domain terms: /doc-meta/glossary.yaml and /docs/02_domain/**
    - service contracts: /docs/03_services/**
- Keep terminology consistent with glossary.

## When code changes
- If behavior, API, data model, tenancy, auth, or ops changes:
    - Update docs in /docs/** accordingly.
    - If this is an architectural decision, create a NEW ADR under /docs/04_decisions/ (never rewrite existing ADRs).

## ADR policy
- Existing ADR files are immutable except for adding an "Errata" section.
- For new decisions, create ADR-XXX with:
    - Status: Proposed/Accepted
    - Context, Decision, Consequences

## Output format in chat
- When you propose doc updates, output only:
    - file path
    - exact section heading
    - the new/changed content
- Do not dump full files unless explicitly asked.