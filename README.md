# Harsh Health Monitor 🏃‍♂️💓🔥

**Harsh Health Monitor** is a professional Android application designed to track, store, and visualize your personal health data using modern Android technologies. It integrates seamlessly with the Android health ecosystem to provide deep insights into your physical well-being.

## 🌟 Key Features

- **Health Connect Integration**: Syncs data from Google Fit, Samsung Health, and other apps.
- **Multi-Metric Tracking**: Monitors **Steps**, **Heart Rate (Avg)**, and **Total Calories Burned**.
- **Local SQL Storage**: Every sync is persisted in a local **Room Database**, building a long-term history on your device.
- **Live Visualizations**: Interactive in-app charts (powered by `MPAndroidChart`) showing your health trends in real-time.
- **Data Export**: Export your entire health history to a **CSV file** for external analysis.
- **Python Visualization**: Includes a custom Python tool to generate professional health reports from your exported data.

## 🛠 Tech Stack

- **Language**: 100% Kotlin
- **Build System**: AGP 9.3 with Built-in Kotlin & KSP
- **Local Storage**: Room Persistence Library (SQL)
- **Data Source**: Health Connect SDK
- **Charts**: MPAndroidChart
- **Analysis**: Python (Pandas + Matplotlib) for external visualization

## 🚀 Getting Started

### Prerequisites
- Android Studio (2026.1.2 or newer recommended)
- A device or emulator with **Health Connect** installed.

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/Harshavardhanreddy2004/Healthmontoring.git
   ```
2. Open the project in Android Studio.
3. Build and run the app on your device.
4. Tap the **"Connect"** button to grant permissions and start syncing data.

## 📊 Data Visualization

### In-App
Simply tap the "Connect" button to refresh the dashboard. The charts will automatically populate with your historical data from the local SQL database.

### External (Python)
1. Use the **"Export to CSV"** button in the app to save your data to the device's Downloads folder.
2. Copy the CSV file to your computer.
3. Run the visualization tool:
   ```bash
   python .artifacts/b9ec85b2-b6ea-492d-b187-425d88a087ee/visualize_health.py your_data.csv
   ```

## 🔒 Privacy
Your health data is yours. This app stores all information locally on your device. No data is shared with external servers unless you manually export it.

---
Created by **Harshavardhan Reddy**
