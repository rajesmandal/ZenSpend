# Implementation Plan - Home/Dashboard UI Redesign

Redesign the ZenSpend Home screen to provide a quick financial health overview while maintaining existing business logic and domain data.

## Proposed Changes

### Dashboard Feature

#### [MODIFY] [DashboardScreen.kt](file:///Users/apple/AndroidStudioProjects/ZenSpend/app/src/main/java/com/android/zenspend/feature/dashboard/presentation/screen/DashboardScreen.kt)
- Redesign the `DashboardContent` to follow the new hierarchy.
- Implement the new **Top Bar** with compact greeting and household indicator.
- Refactor **Net Worth Card** to use `AmountLarge` and show Assets/Liabilities clearly.
- Create a new **Monthly Money Flow** section that is compact and uses semantic colors.
- Update **Budget Status** to use a progress bar and clear remaining amount.
- Implement **Spending Breakdown** showing top 3-4 categories with horizontal bars.
- Redesign **Upcoming** section to show actionable items (EMI, Recurring) compactly.
- Redesign **Recent Transactions** with a clean list of latest 5 items.
- Implement proper **Empty States** for all sections.
- Ensure responsive design and accessibility.

#### [MODIFY] [DashboardPreviews.kt](file:///Users/apple/AndroidStudioProjects/ZenSpend/app/src/main/java/com/android/zenspend/feature/dashboard/presentation/screen/DashboardPreviews.kt)
- Update previews to reflect the new UI.

### Design System (if needed)

#### [MODIFY] [Cards.kt](file:///Users/apple/AndroidStudioProjects/ZenSpend/app/src/main/java/com/android/zenspend/core/ui/designsystem/component/Cards.kt)
- Ensure `ZenCard` matches the "rounded cards with subtle elevation/borders" requirement.

## Verification Plan

### Automated Tests
- Run `DashboardAccountingTest` and `DashboardViewModelTest` to ensure no regression in logic.
- Verify the build completes successfully.

### Manual Verification
- Use Compose Previews to verify:
    - Light/Dark mode appearance.
    - Responsive layout on different screen sizes.
    - Empty states (using mock states in previews).
- Deploy to an emulator to verify:
    - Performance and animations.
    - Accessibility (TalkBack, font scaling).
    - Compact and readable financial numbers.
