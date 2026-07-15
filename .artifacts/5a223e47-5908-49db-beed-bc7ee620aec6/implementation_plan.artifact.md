# Implementation Plan - Read Data from Health Connect

This plan outlines the steps to implement reading step count data from the Health Connect app. We will extend the existing `MainActivity` to fetch the total steps for the current day once permissions are granted.

## Proposed Changes

### UI Update

#### [MODIFY] [activity_main.xml](file:///C:/Users/Harsh/AndroidStudioProjects/HealthMonitor/app/src/main/res/layout/activity_main.xml)
- Add a `TextView` with ID `steps_text` to display the retrieved step count.

### Logic Update

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Harsh/AndroidStudioProjects/HealthMonitor/app/src/main/java/com/example/healthmonitor/MainActivity.kt)
- Add a private function `readStepsData()` that uses `healthConnectClient.aggregate` to fetch `StepsRecord.COUNT_TOTAL`.
- Update the UI with the result.
- Call `readStepsData()` after permissions are confirmed (both in the callback and the initial check).
- Import necessary classes: `java.time.Instant`, `java.time.temporal.ChronoUnit`, `androidx.health.connect.client.request.AggregateRequest`, `androidx.health.connect.client.time.TimeRangeFilter`.

## Verification Plan

### Automated Tests
- I will run `gradle_sync` to ensure imports are resolved correctly.
- I will run `gradle_build(":app:assembleDebug")` to verify there are no compilation errors.

### Manual Verification
- The user can run the app on a device/emulator with Health Connect installed and data available.
- Upon clicking "Connect" and granting permissions, the "Steps" count should update from "Steps: --" to the actual count (or 0 if no data).
