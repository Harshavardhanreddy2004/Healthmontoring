"""
Harsh Health Monitor - External Visualization Tool

This script takes a CSV file exported from the Harsh Health Monitor app
and generates a multi-panel visualization of Steps, Distance, Heart Rate, and Calories.

Usage:
    python visualize_health.py <path_to_csv_file>

Requirements:
    pip install pandas matplotlib
"""

import pandas as pd
import matplotlib.pyplot as plt
from datetime import datetime
import sys
import os

def visualize(csv_path):
    if not os.path.exists(csv_path):
        print(f"Error: File {csv_path} not found.")
        return

    # Load data
    df = pd.read_csv(csv_path)

    # Convert timestamp to datetime
    df['Date'] = pd.to_datetime(df['Timestamp'], unit='ms')
    df = df.sort_values('Date')

    # Create plots
    fig, (ax1, ax2, ax3, ax4) = plt.subplots(4, 1, figsize=(10, 14), sharex=True)

    # Steps Plot
    ax1.bar(df['Date'], df['Steps'], color='skyblue', label='Steps')
    ax1.set_ylabel('Steps')
    ax1.set_title('Harsh Health Monitor Data Visualization')
    ax1.legend()

    # Distance Plot
    ax2.plot(df['Date'], df['Distance'], color='purple', marker='v', label='Distance (km)')
    ax2.set_ylabel('Distance')
    ax2.legend()

    # Heart Rate Plot
    ax3.plot(df['Date'], df['HeartRate'], color='red', marker='o', label='Heart Rate (Avg)')
    ax3.set_ylabel('BPM')
    ax3.legend()

    # Calories Plot
    ax4.plot(df['Date'], df['Calories'], color='green', marker='s', label='Calories (kcal)')
    ax4.set_ylabel('kcal')
    ax4.set_xlabel('Date')
    ax4.legend()

    plt.xticks(rotation=45)
    plt.tight_layout()

    output_img = "health_plot.png"
    plt.savefig(output_img)
    print(f"Visualization saved as {output_img}")
    plt.show()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python visualize_health.py <path_to_csv>")
    else:
        visualize(sys.argv[1])
