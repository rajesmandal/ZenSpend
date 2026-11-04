# ZenSpend — "Bento" Design System

Redesign of the whole app UI in the **Bento** style (cream canvas, black + lime contrast,
pastel tiles, pill dock, Space Grotesk numerals). Prototypes that this implements live in
`prototype/` (`zenspend-redesign-bento.html` is the chosen one).

> **Golden rule:** screens never contain raw colours, font names, dp radii or user-visible text.
> They ask the design system for a *role* (a tone, a style, a dimension, a string resource).
> To change how the app looks, you change the design system — not the screens.

---

## 1. Where to change what

| I want to change…                         | Edit this                                                            |
|-------------------------------------------|----------------------------------------------------------------------|
| Any colour / the whole palette            | `core/ui/theme/Color.kt` (the **only** file with hex values)         |
| Which palette colour fills which role     | `core/ui/theme/Theme.kt` (Material scheme + `CustomColors`)          |
| Pastel tile colours, tile text colours    | `core/ui/theme/Tone.kt` (`lightTonePalette()` / `darkTonePalette()`) |
| Fonts (display vs body)                   | `core/ui/theme/Type.kt` → `DisplayFontFamily` / `BodyFontFamily`     |
| Text sizes, weights, letter-spacing       | `core/ui/theme/Type.kt`                                              |
| Spacing, radii, dock/button/badge sizes   | `core/ui/theme/Dimensions.kt`                                        |
| Shapes (pill, tile, row…)                 | `core/ui/theme/Shape.kt`                                             |
| Text shown to the user                    | `res/values/strings.xml`                                             |
| Bottom dock tabs / primary "+" action     | `core/navigation/NavigationChrome.kt` + `dockItems` in `ZenSpendApp` |

Examples:
* *"Make the accent orange instead of lime"* → change `Lime` in `Color.kt` (and nothing else; dock
  highlight, primary buttons, budget tile and chips follow).
* *"Less rounded look"* → lower `RadiusTile`, `RadiusCard`, `RadiusRow` in `Dimensions.kt`.
* *"Use a different display font"* → drop the font into `res/font/`, point `SpaceGroteskFontFamily`
  (or a new family) at it in `Type.kt`, and set `DisplayFontFamily` to it.

---

## 2. Layers

```
core/ui/theme/            tokens          Color · Tone · Theme · Type · Dimensions · Shape
core/ui/designsystem/     components      Tiles · Dock · Keypad · Cards · Buttons · ShellComponents · …
core/ui/util/             helpers         Formatting (money, greeting, relative date)
feature/*/presentation/   screens         compose tokens + components, no styling decisions
```

Data flow is unchanged: `ViewModel → UiState (contract) → Screen`. The redesign is purely in the
presentation layer; domain/data/DI were not touched.

### Colour model
1. `Color.kt` — raw palette (`Cream`, `Ink`, `Lime`, `Coral`, …), all `internal`.
2. `Theme.kt` maps them to Material roles (`primary = Ink`, `onPrimary = Lime`,
   `background = Cream`, `surfaceContainerLow = Paper`, …) for light **and** dark.
3. Screens read **only** `MaterialTheme.colorScheme.*` or `ZenSpendTheme.colors.*`.

Because `primary` = Ink / `primaryContainer` = Lime, every un-redesigned screen (buttons, switches,
dialogs, text fields) automatically picks up the new look.

### Tones (`ZenTone`)
A tone = a named container colour with matching content colours.
`Ink, Lime, Coral, Lilac, Sky, Sun, Mint, Surface`.

```kotlin
ZenTile(tone = ZenTone.Lime) { … }                 // tile
val c = ZenSpendTheme.colors.tone(ZenTone.Coral)    // c.container / c.content / c.contentMuted
ZenTone.forKey(category.name)                       // stable pastel per category / wallet (no per-item hardcoding)
```

Pastel tones always carry Ink text (works in light and dark). `Ink` flips to a raised grey in dark
mode; `Surface` is white / dark card.

---

## 3. Component catalogue (`core/ui/designsystem/component`)

| Component | File | Purpose |
|-----------|------|---------|
| `ZenTile`, `ZenTileCaption`, `ZenTileValue`, `ZenArrowBadge`, `ZenStat` | Tiles.kt | The bento tile and its inner pieces; children auto-match the tile tone |
| `ZenBadge` | Tiles.kt | Rounded coloured badge with first letter or an icon |
| `ZenSectionHeader` | Tiles.kt | Section title + optional action |
| `ZenProgressBar`, `ZenRingProgress` | Tiles.kt | Thick bar / ring, animated, tone-aware |
| `ZenChip`, `ZenChipRow` | Tiles.kt | Pill filters (replaces tab rows) |
| `ZenTransactionRow` | Tiles.kt | Standard list row (transactions, wallets, upcoming) |
| `ZenNavigationBar`, `ZenNavItem`, `ZenDockAction` | Dock.kt | Floating pill dock, selected tab expands to show label, round "+" |
| `ZenKeypad`, `applyKeypadKey`, `KeypadKey` | Keypad.kt | Amount keypad + pure reducer (unit-tested) |
| `ZenCard`, `ZenSurfaceCard`, `SummaryCard` | Cards.kt / ShellComponents.kt | Legacy cards, restyled to bento surface |
| `ZenPrimaryButton`, `ZenSecondaryButton` | Buttons.kt | Pill buttons (ink + lime) |
| `ZenSegmentedControl` | ShellComponents.kt | Pill segmented control |
| `ZenTopAppBar` | ShellComponents.kt | Large display-font title |
| `ZenTextField`, `AmountInput` | TextFields.kt | Inputs using `InputShape` |

Adding a new screen? Compose it from these. If a pattern repeats twice, make it a component here.

---

## 4. Screens redesigned

| Screen | File | Notes |
|--------|------|-------|
| Dashboard | `feature/dashboard/.../DashboardScreen.kt` | Bento grid: Net-worth hero (Ink) with Add/Wallets/Cards actions, Income (Lime), Spent (Coral), Saved (Mint), Budget (Lilac), AI (Sky), SMS (Sun), top spending, upcoming, recent |
| Transactions (Activity) | `feature/transaction/.../TransactionListScreen.kt` | Filter chips (All/Expense/Income/Transfer), grouped by day, `ZenTransactionRow` |
| Add / Edit transaction | `feature/transaction/.../TransactionAddEditScreen.kt` | Type chips, big amount read-out, **keypad**, category chips (`CategoryChips`) |
| Budgets | `feature/budget/.../BudgetListScreen.kt` | Lime ring overview + 2-column category tiles, "OVER" tag |
| Insights (Reports) | `feature/report/.../ReportScreen.kt`, `ReportComponents.kt` | Chip tabs, summary tiles, category rows, Ink trend tile |
| Wallets | `feature/wallet/.../WalletListScreen.kt` | Net-worth / assets / liabilities tiles + account rows |
| Settings | `feature/settings/.../SettingsScreen.kt` | Section headers + tone badges (via `ZenListRow`), dividers removed |
| Credit cards (list) | `feature/creditcard/.../CreditCardListScreen.kt` | One tone tile per card (`ZenTone.forKey(id)`), outstanding/available, utilization bar |
| Credit card details | `feature/creditcard/.../CreditCardDetailsScreen.kt` | Ink outstanding tile, EMIs as tone tiles |
| SMS drafts | `feature/sms/.../SmsDraftListScreen.kt` | Sun "waiting" tile + draft tiles with Ignore / Review |
| AI assistant | `feature/ai/.../AiAssistantScreen.kt` | Ink/Sky chat bubbles, `ZenChip` suggestions |
| Wallet details | `feature/wallet/.../WalletDetailsScreen.kt` | Ink hero tile + `TransactionItem` rows |

**All other screens** (EMI, recurring, SMS review, backup, premium, login, shared
cashbook, wallet/budget/card forms, transaction/budget/recurring details) are *not* individually redesigned yet,
but they already use the new colours, fonts, shapes, buttons, text fields, cards and top bar because
they go through the shared theme/components. Redesigning them = swapping their layout to `ZenTile`s.

---

## 5. Architecture / navigation changes

1. **Bottom nav → floating dock.** `ZenNavigationBar` now takes an optional `ZenDockAction`
   (the round coral "+"), and `selectedIndex` may be `-1`.
2. **Dock has 4 tabs, not 5:** Home · History · Wallets · Insights. The old "More" tab (Settings) was
   removed from the dock; Settings is opened from the **gear icon in the Dashboard header**
   (it still shows the dock, with no tab highlighted).
3. **FAB removed.** The "+" lives in the dock. `NavigationChrome` replaced
   `showsFab/fabDestination/fabLabel` with `dockRoutes` and `primaryActionDestination(route)`
   (Wallets tab → Add wallet, everything else → Add transaction).
4. **Budgets are now reachable.** The `Budgets` route existed but nothing navigated to it; the
   Dashboard budget tile now opens it.
5. **`DashboardScreen` takes a `DashboardNavigation` object** instead of a growing list of lambdas.
   Add new dashboard destinations as fields there.
6. **`ZenNavItem` takes a `@StringRes` label** (was a `String`) so nav labels are localisable.
7. **`MonthSelector`** moved to a shared, restyled composable (still in `DashboardScreen.kt`) used by
   Dashboard, Budgets and Insights.
8. New shared helpers in `core/ui/util/Formatting.kt`:
   `formatMinorUnits`, `greetingText`, `relativeDateText`.

No database, repository, use-case, DI or ViewModel code changed.

---

## 6. Conventions (keep the design maintainable)

* **No hex colours outside `Color.kt`.** No `Color.Red/Gray/…`.
* **No literal dp for design decisions** in screens — use `Dimensions.*`. (Tiny structural values such as
  a 10.dp legend dot are acceptable.)
* **No `fontFamily`/`fontSize` in screens** — use `MaterialTheme.typography.*` or the `Amount*` styles.
* **No user-visible literals** in redesigned screens — add to `strings.xml`. Money uses
  `R.string.amount_rupees` (`₹%1$s`); a future multi-currency change is then one string/one helper.
* **Category/wallet colours come from `ZenTone.forKey(...)`**, never a per-category colour table.
* **Prefer a tone over a colour**: `ZenTile(tone = …)`, `ZenBadge(tone = …)`.
* Dark mode is part of every token — never special-case it in a screen.

---

## 7. Known limitations / follow-ups

* **Space Grotesk is a variable font.** On Android 8+ (API 26+) the weights render correctly; on
  API 24–25 the system falls back to the font's default weight. Add static weight files
  (`space_grotesk_bold.ttf`, …) to `res/font/` and list them in `SpaceGroteskFontFamily` if low-API
  fidelity matters.
* Currency symbol is still `₹` inside `strings.xml`; non-redesigned screens still hardcode it.
* The Dashboard bell button is a no-op placeholder (as before).
* Transactions screen has no net-for-month summary tile (the list model only exposes pre-formatted
  amount strings). Add a numeric field to `TransactionUiModel` or a summary to the state to enable it.
* `ZenChipRow` filters on the Activity list are client-side over the loaded list.
* Not yet verified on a device/emulator: only compile, unit tests and APK assembly were run
  (see below). Please eyeball each screen in light + dark and at large font sizes.
* `androidTest` sources were not compiled here (its dependencies aren't in the offline Gradle
  cache); a grep found no references to changed APIs.

---

## 8. Verification done

* `./gradlew :app:compileDebugKotlin :app:assembleDebug :app:testDebugUnitTest --offline` — **passes**
  (including 7 new tests in `KeypadTest`).
* Audit: no raw `Color(0x…)` outside `Color.kt`; no hardcoded `₹` strings in redesigned screens.
