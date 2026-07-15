# Contributing to Harsh Health Monitor

We welcome contributions to make this health tracking experience even better!

## Project Structure

- `app/src/main/java/com/harsh/healthmonitor/`: Main Kotlin source code.
- `app/src/main/java/com/harsh/healthmonitor/data/`: Room Database Entity, DAO, and Database definition.
- `app/src/main/res/layout/`: UI XML layouts.
- `.artifacts/`: External tools and implementation records.

## How to Contribute

1.  **Fork the Project**: Create a copy of the repository in your GitHub account.
2.  **Create a Branch**: `git checkout -b feature/AmazingFeature`
3.  **Commit Changes**: Make your changes and commit them with descriptive messages.
4.  **Push**: `git push origin feature/AmazingFeature`
5.  **Pull Request**: Open a PR back to the main repository.

## Adding New Health Metrics

If you want to add a new metric (e.g., Sleep or Blood Pressure):
1.  Add the new permission to `AndroidManifest.xml`.
2.  Update the `HealthRecord` entity and `HealthDao`.
3.  Modify `MainActivity.kt` to fetch and display the new data.
4.  Update the UI in `activity_main.xml`.

## Code Style
Please follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
