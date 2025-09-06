# Coding Rules

### Terminology
- Required / Prohibited: this is must. In other words, you absolutely have to do so.
- Should be done / Should be avoided: this is should. In other words, it is preferable to do so, but edge cases ought to be allowed.
- Desirable / Better not to do: this is idealism. In other words, it means you should aim for it.

## Design

As a base, we follow the principles of Clean Architecture. That is:
- ### Encapsulation, Abstraction, and Dependency Inversion:
  1. `infrastructure.dao.*`, which directly accesses DB/File (IO), must be hidden behind `domain.repository.*` (interfaces).
  2. `domain.usecase.*` must not access dao directly. If you want to access dao in usecase, you need to go through the repository.
  3. Implementations of repositories must be placed in `infrastructure.repository.*`.
  4. Data passed between classes/instances/functions must be defined as data class / value object under `domain.[entity/vo]`. 
  However, as long as the passing occurs within a single class, it should be permissible to define it within the class.
  5. In `presentation.*`, Listener and Controller must be separated. The Listener should focus on early returns and event decomposition—that is, extracting fields from the Event object and passing only what is necessary to the Controller’s functions.
  6. The principle of the least privilege must be strictly observed. For example, unless the Controller needs to be involved with `event.status`, passing `event.status` is prohibited. Passing the entire `event` itself is also prohibited.
  7. Controller bloat should be avoided. Break it down properly into use cases, and restrict its business to operating the parent API only.
  8. `usecase` should, when necessary (for example, when multiple error are involved behind it) return `sealed interface [UseCase].Result.[Success(val data; if necessary) / Failure.Reason( val data; if necessary)]` as its result.
  9. Anything related to dependency injection must be placed under `.di.*`.
  10. Static, side-effect-free code that may be used from all layers (e.g., algorithms, helpers, extensions) desirable to placed under `.utils.*`. 
  11. However, as with all of these, if the project becomes too large, you should separate the modules. If utils becomes a garbage can, it's time to separate the modules.

- ### Injection:
  1. Instances such as Controllers and Usecases must be injected via KSP + Dagger.
  2. When appropriate, being a Singleton is desirable.

## Implementation

- ### Style:
  1. As a rule, do not write code comments; it is desirable to write code whose intent and behavior can be understood just by reading the code, variable names, and function names. However, non-obvious trade-offs, constraints, and the intent of special algorithms should be left as comments.
  2. Do not merely follow instructions; if there is something you do not understand, ask questions, propose better solutions, and code with responsibility and initiative not just as an agent but as a colleague.
  3. Variable names should not be abbreviated. For example, use `event: InputEvent` instead of `e: InputEvent` (at least when what `event` refers to is unique within the function. If not, use something like `inputEvent`). However, common abbreviations such as `url`, `io`, and `id` are permitted.
  4. Coding conventions must follow the official Kotlin guidelines.

- ### Principle:
  1. Reduce side effects as much as possible. [Should be done]
  This also ties into system design, mixing business logic and I/O is Better not to do.
  For example, delegate validation to the entity data class’s companion object or `@JvmInline value class` so that a single function does not become bloated.
  2. Be mindful of scope and extract private functions. [Should be done]
  Keep variable scopes small.
  Functions with a narrowly bounded scope are easier to understand and simpler to modify.
  3. DRY. [Required]
  Unless there is a justified reason, duplicate code must be reduced or eliminated. But watch the classic trap: over-abstraction. Duplication is acceptable until the third repetition makes the abstraction obvious.

- ### Niche:
  1. In implementations that involve blocking I/O, `withContext(Dispatchers.IO)` should be done by the RepositoryImpl.
  2. You must always run detekt before committing.


### Appendix — Bukkit-Native Pragmatism (Project-specific)

- Treat Bukkit as the native platform
- Fast-path rule (no domain leakage):
  If both the inputs and outputs are Bukkit types *and* the operation does not apply domain rules, pass Bukkit objects directly (e.g., `World`, `Chunk`, `Entity`, `Player`) between Presentation and `infrastructure.bukkit.service`. In this case, that package is a helper; do **not** introduce a domain wrapper or a repository/port just for purity. Never re-fetch a high-cost entity (`Chunk`, `World`, `Entity`) you already have.
- Domain boundary:
  `domain.*` must not depend on Bukkit types. Keep it to value objects, entities, and pure functions. Encoding/decoding, threads, lifecycles, and any I/O stay outside the domain. However, framework objects that do not involve "threads," "lifecycles," or "I/O" and have few side effects are allowed to be placed in the domain. For example, ItemStack (read-only), Material, etc.
- External integrations:  
  When a use case talks to “outsiders” (DB/FS/network or other plugins like WorldEdit), revert to standard Clean Architecture: define ports under `domain.repository.*` and put implementations under `infrastructure.repository.*`. Blocking I/O belongs behind repositories.

