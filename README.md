# Angular CLI QuickSwitch

Quickly switch between related component files in IntelliJ-based IDEs. Use one shortcut to cycle through a component, or jump directly to its controller, markup, style, or test files.

Given these related files:

```text
foo/
    foo.component.css
    foo.component.html
    foo.component.spec.ts
    foo.component.ts
```

Press **Alt+S** repeatedly to cycle through the enabled file types:

```text
foo.component.ts -> foo.component.html -> foo.component.css -> foo.component.ts
```

Test files are excluded from the main cycle by default. Press **Alt+T** to jump directly to `foo.component.spec.ts`, or enable test files in the plugin settings to include them in the main cycle.

## Shortcuts

| Action | Default | Behavior |
|---|---|---|
| QuickSwitch | **Alt+S** | Cycles through all enabled related file types. |
| QuickSwitch: Tests | **Alt+T** | Jumps to and cycles between `.spec.ts` and `.spec.js`. |
| QuickSwitch: Controllers | **Alt+X** | Jumps to and cycles between `.ts` and `.js`. |
| QuickSwitch: Styles | **Alt+C** | Jumps to and cycles between supported style files. |
| QuickSwitch: Markup | **Alt+V** | Jumps to and cycles between supported markup files. |

All shortcuts can be changed under the IDE's **Settings | Keymap** page by searching for `QuickSwitch`.

## Settings

Open **Settings | Tools | Angular CLI QuickSwitch** to configure navigation behavior and the main cycle.

### General

**Close the previous tab after switching** is disabled by default.

When disabled, the previous file remains open. This provides the smoothest behavior and preserves tab order, pinned state, and editor splits.

When enabled, QuickSwitch opens the target in the active editor group first and closes the source only after the target opens successfully. Opening first minimizes the tab collapse and flicker caused by the old close-first behavior.

IntelliJ does not expose stable public APIs for transferring pinned state or closing only one occurrence of a file in a specific split. Consequently, close-on-switch mode can lose pinning and may close other occurrences of the source file in sibling editor groups.

### Supported Extensions

The settings page groups file types by purpose:

| Group | File types | Main-cycle default |
|---|---|---|
| Controller | `.ts`, `.js` | Enabled |
| Template | `.html`, `.php`, `.haml`, `.jade`, `.pug`, `.slim` | Enabled |
| Style | `.css`, `.sass`, `.scss`, `.less`, `.styl` | Enabled |
| Tests | `.spec.ts`, `.spec.js` | Disabled |

Each checkbox controls whether that file type participates in the main **Alt+S** cycle. This makes workflows such as cycling directly between TypeScript and HTML possible by disabling the style types.

The dedicated category shortcuts intentionally ignore these checkboxes. For example, **Alt+T** can always find a related test file even when tests are excluded from the main cycle.

If the current file type is disabled, QuickSwitch can still switch away from it to the next enabled counterpart. Disabled types are only excluded as main-cycle targets.

## Compatibility

Version 2.0 targets IntelliJ Platform build `242` and newer, corresponding to IntelliJ IDEA 2024.2 and compatible releases of WebStorm, PhpStorm, PyCharm, GoLand, Rider, CLion, Android Studio, and other standalone IntelliJ-based IDEs.

The plugin is compiled with Java 21. End users do not need to install a separate JDK because supported IDEs provide the required runtime. Java 21 is required when building the plugin from source.

Compatibility is checked with JetBrains Plugin Verifier against platform versions from 2024.2 through 2026.2.

## License

Licensed under the [Apache License 2.0](LICENSE).
