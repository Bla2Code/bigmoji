<!--
SYNC IMPACT REPORT
==================
Version change: N/A → 1.0.0 (initial constitution)
Added principles:
  - I. Code Quality Standards
  - II. Testing Standards
  - III. User Experience Consistency
  - IV. Performance Requirements
Added sections:
  - Development Workflow
  - Quality Gates
Templates requiring updates:
  - .specify/templates/plan-template.md: ✅ aligned (Constitution Check section present)
  - .specify/templates/spec-template.md: ✅ aligned (requirements section compatible)
  - .specify/templates/tasks-template.md: ✅ aligned (task categorization supports principle-driven types)
Follow-up TODOs: None
-->

# bigmoji Constitution

## Core Principles

### I. Code Quality Standards

All code MUST adhere to established quality benchmarks to ensure maintainability, readability, and reliability.

- **Linting & Formatting**: Every file MUST pass linting and formatting checks before merge. Configure and enforce tools appropriate to the language (e.g., ruff/black for Python, eslint/prettier for JavaScript, clippy/rustfmt for Rust).
- **Code Reviews**: All changes MUST be reviewed by at least one team member before merging. Reviewers verify correctness, clarity, and adherence to principles.
- **Simplicity First**: Code MUST be as simple as possible while meeting requirements. Avoid premature optimization and unnecessary abstraction layers. YAGNI principles apply.
- **Documentation**: Public APIs, modules, and non-obvious logic MUST be documented. Documentation lives close to the code it describes and is kept up to date.
- **Error Handling**: All errors MUST be handled explicitly. Failures should produce clear, actionable messages. Silent failures are unacceptable.

**Rationale**: Consistent code quality reduces cognitive load, accelerates onboarding, and prevents defects from reaching production.

### II. Testing Standards

Testing is NON-NEGOTIABLE. Every feature and fix MUST include appropriate test coverage.

- **Test-First Approach**: Tests SHOULD be written before implementation where practical. The Red-Green-Refactor cycle is encouraged but not strictly mandated.
- **Coverage Requirements**: New code MUST achieve at least 80% line coverage. Critical paths (authentication, data persistence, API contracts) MUST have 100% coverage.
- **Test Types**:
  - **Unit Tests**: Every function and method MUST have unit tests covering normal and edge cases.
  - **Integration Tests**: All inter-component communication and external API interactions MUST have integration tests.
  - **Contract Tests**: API endpoints and public interfaces MUST have contract tests ensuring compatibility.
- **Test Independence**: Each test MUST run independently and produce consistent results. No test ordering dependencies allowed.
- **CI Enforcement**: All tests MUST pass in continuous integration before merge. Flaky tests MUST be fixed or removed promptly.

**Rationale**: Comprehensive testing provides confidence for refactoring, prevents regressions, and serves as living documentation of expected behavior.

### III. User Experience Consistency

User-facing features MUST deliver a consistent, predictable, and polished experience across all interaction points.

- **Visual Consistency**: UI elements MUST follow established design patterns and style guides. Emojis and visual assets MUST render correctly across supported platforms (Discord desktop, mobile, web).
- **Behavioral Consistency**: Similar actions MUST produce similar results. User feedback (loading states, success/error messages) MUST be uniform throughout the application.
- **Accessibility**: Features MUST be usable by all users. Support keyboard navigation, screen readers, and high-contrast modes where applicable.
- **Error Communication**: User-facing errors MUST be clear, actionable, and non-technical. Never expose raw error messages or stack traces to end users.
- **Performance Perception**: Perceived performance matters as much as actual performance. Use loading indicators, progressive rendering, and optimistic updates to maintain responsiveness.

**Rationale**: Consistent user experience builds trust, reduces confusion, and ensures the product feels professional and reliable.

### IV. Performance Requirements

The system MUST meet defined performance thresholds to ensure responsiveness and scalability.

- **Response Time**: API responses MUST complete within 200ms at p95 under normal load. Discord bot interactions MUST respond within Discord's 3-second interaction window.
- **Resource Efficiency**: Memory usage MUST remain stable under sustained load. No memory leaks permitted. CPU usage MUST scale linearly with request volume.
- **Scalability**: System MUST handle expected load with 2x headroom. Load testing MUST be performed before major releases.
- **Asset Optimization**: Images, emojis, and other assets MUST be optimized for size without quality loss. Use appropriate formats (WebP, AVIF) and lazy loading where applicable.
- **Monitoring & Alerting**: Performance metrics MUST be collected and monitored. Alerts MUST trigger when thresholds are approached, not exceeded.

**Rationale**: Performance directly impacts user satisfaction and system reliability. Proactive performance management prevents issues before they affect users.

## Development Workflow

All development MUST follow established workflow practices to maintain code quality and team coordination.

- **Branch Strategy**: Feature branches MUST be created from the main branch. Branch names MUST follow the pattern `[###-feature-name]`.
- **Commit Standards**: Commits MUST follow conventional commit format (`type: description`). Each commit MUST represent a logical, atomic change.
- **Pull Requests**: PRs MUST include a clear description, link to the relevant spec/issue, and evidence of testing (test output, screenshots for UI changes).
- **Merge Requirements**: All CI checks MUST pass, at least one approval MUST be received, and no unresolved conversations MAY remain before merge.

## Quality Gates

Every change MUST pass through quality gates before reaching production.

- **Pre-commit**: Linting, formatting, and type checking MUST pass locally before push.
- **CI Pipeline**: All tests (unit, integration, contract) MUST pass. Code coverage MUST meet thresholds. Security scanning MUST show no critical vulnerabilities.
- **Pre-deployment**: Performance benchmarks MUST be verified. Manual testing checklist MUST be completed for user-facing changes.
- **Post-deployment**: Health checks MUST pass. Monitoring dashboards MUST show normal metrics for at least 15 minutes after deployment.

## Governance

This constitution supersedes all other development practices and guidelines. Amendments require documentation, team discussion, and approval.

- **Amendment Process**: Propose changes via pull request to this file. Include rationale, impact assessment, and migration plan if applicable. Team consensus is preferred; maintainer has final decision.
- **Compliance Review**: All PRs and code reviews MUST verify compliance with these principles. Violations MUST be documented and justified in the PR description.
- **Versioning Policy**: This constitution follows semantic versioning:
  - **MAJOR**: Backward-incompatible principle removals or redefinitions
  - **MINOR**: New principles added or existing guidance materially expanded
  - **PATCH**: Clarifications, wording improvements, typo fixes
- **Complexity Justification**: Any deviation from these principles MUST be justified with clear reasoning. The burden of proof is on the proposer.

**Version**: 1.0.0 | **Ratified**: 2026-06-03 | **Last Amended**: 2026-06-03
