> [!IMPORTANT]
> By submitting a contribution, you agree to license your contribution under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).
> You also grant the Maintainers a perpetual, worldwide, non-exclusive, irrevocable, royalty-free license to relicense and sublicense your contribution, in whole or in part, under one or more OSI-approved open source licenses (e.g., MIT, BSD-2-Clause, BSD-3-Clause, Apache-2.0). For clarity, relicensing under proprietary or closed-source terms is not permitted.
>

# Contributing

## Code Style

* The coding rules live in **AGENT.md**. They’re written for LLMs but are human-readable. please follow them.

## Commit Messages (soft guideline)

* When possible, use this pattern:

  ```
  [Update|Add|Fix|Remove|Refactor|...]: [root|internal|api][parent/filename] Short description
  ```
* Examples:

  * `Fix: [api][bukkit/service/ActionRunner] false check in dispatch`
  * `Add: [root][detekt.yml] add detekt config`
  * `Refactor: [internal][dao/DefinitionYamlDao] simplify refresh logic`

## Static Analysis

* **Before contributing, you must run Detekt and bring errors down to 0.**
* Do **not** open a PR until Detekt reports no errors.

## Pull Requests

* Write a detailed PR description:

  * What & why (context and motivation)
  * How (key changes)
  * Breaking changes / migration notes
  * Tests & risks
  * Screenshots or logs when helpful
* Link related issues. Explain trade-offs if any.

## Quick Checklist

* [ ] Read **AGENT.md**
* [ ] Follow the commit message pattern (if possible)
* [ ] Run Detekt → **0 errors**
* [ ] Write a clear, detailed PR message

Thanks for contributing! 🎉
