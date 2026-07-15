package com.harsh.healthmonitor

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.harsh.healthmonitor.data.AppDatabase
import com.harsh.healthmonitor.data.HealthRecord
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var stepsText: TextView
    private lateinit var heartRateText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var connectButton: Button
    private lateinit var exportButton: Button

    private lateinit var stepsChart: BarChart
    private lateinit var heartRateChart: LineChart
    private lateinit var caloriesChart: LineChart

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(this) }
    private val database by lazy { AppDatabase.getDatabase(this) }

    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(permissions)) {
            statusText.text = "Health Connect: Permissions Granted"
            readHealthData()
        } else {
            statusText.text = "Health Connect: Permissions Denied"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)
        stepsText = findViewById(R.id.steps_text)
        heartRateText = findViewById(R.id.heart_rate_text)
        caloriesText = findViewById(R.id.calories_text)
        connectButton = findViewById(R.id.connect_button)
        exportButton = findViewById(R.id.export_button)

        stepsChart = findViewById(R.id.steps_chart)
        heartRateChart = findViewById(R.id.heart_rate_chart)
        caloriesChart = findViewById(R.id.calories_chart)

        setupCharts()
        loadHistoricalData()

        connectButton.setOnClickListener {
            checkAvailabilityAndRequestPermissions()
        }

        exportButton.setOnClickListener {
            exportToCSV()
        }
    }

    private fun checkAvailabilityAndRequestPermissions() {
        val availabilityStatus = HealthConnectClient.getSdkStatus(this)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            statusText.text = "Health Connect is not available on this device"
            return
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            statusText.text = "Health Connect update required"
            val uriString = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
                putExtra("overlay", true)
                putExtra("callerId", packageName)
            })
            return
        }

        lifecycleScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(permissions)) {
                statusText.text = "Health Connect: Permissions Already Granted"
                readHealthData()
            } else {
                requestPermissions.launch(permissions)
            }
        }
    }

    private fun readHealthData() {
        lifecycleScope.launch {
            try {
                val endTime = Instant.now()
                val startTime = endTime.truncatedTo(ChronoUnit.DAYS)
                val response = healthConnectClient.aggregate(
                    AggregateRequest(
                        metrics = setOf(
                            StepsRecord.COUNT_TOTAL,
                            HeartRateRecord.BPM_AVG,
                            TotalCaloriesBurnedRecord.ENERGY_TOTAL
                        ),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                val stepCount = response[StepsRecord.COUNT_TOTAL] ?: 0
                val avgHeartRate = response[HeartRateRecord.BPM_AVG] ?: 0L
                val energyTotal = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0

                stepsText.text = "Steps: $stepCount"
                heartRateText.text = "Heart Rate: $avgHeartRate bpm"
                caloriesText.text = "Calories: ${String.format(Locale.US, "%.1f", energyTotal)} kcal"

                // Store in local SQL database
                database.healthDao().insertRecord(
                    HealthRecord(
                        timestamp = endTime.toEpochMilli(),
                        steps = stepCount,
                        heartRate = avgHeartRate.toDouble(),
                        calories = energyTotal
                    )
                )
                loadHistoricalData()
            } catch (e: Exception) {
                statusText.text = "Error reading health data: ${e.message}"
            }
        }
    }

    private fun setupCharts() {
        val textColor = ContextCompat.getColor(this, android.R.color.black)

        listOf(stepsChart, heartRateChart, caloriesChart).forEach { chart ->
            chart.description.isEnabled = false
            chart.setNoDataText("Sync with Health Connect to see charts")
            chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            chart.xAxis.setDrawGridLines(false)
            chart.xAxis.textColor = textColor
            chart.axisLeft.textColor = textColor
            chart.axisRight.isEnabled = false
            chart.legend.textColor = textColor

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                private val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
                override fun getFormattedValue(value: Float): String {
                    return Instant.ofEpochMilli(value.toLong())
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                }
            }
        }
    }

    private fun loadHistoricalData() {
        lifecycleScope.launch {
            val records = database.healthDao().getAllRecords().reversed()
            if (records.isNotEmpty()) {
                updateStepsChart(records)
                updateHeartRateChart(records)
                updateCaloriesChart(records)
            }
        }
    }

    private fun updateStepsChart(records: List<HealthRecord>) {
        val entries = records.map { BarEntry(it.timestamp.toFloat(), it.steps.toFloat()) }
        val dataSet = BarDataSet(entries, "Steps")
        dataSet.color = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        dataSet.valueTextColor = ContextCompat.getColor(this, android.R.color.black)

        stepsChart.data = BarData(dataSet)
        stepsChart.invalidate()
    }

    private fun updateHeartRateChart(records: List<HealthRecord>) {
        val entries = records.map { Entry(it.timestamp.toFloat(), it.heartRate.toFloat()) }
        val dataSet = LineDataSet(entries, "Heart Rate")
        dataSet.color = ContextCompat.getColor(this, android.R.color.holo_red_light)
        dataSet.setCircleColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(false)

        heartRateChart.data = LineData(dataSet)
        heartRateChart.invalidate()
    }

    private fun updateCaloriesChart(records: List<HealthRecord>) {
        val entries = records.map { Entry(it.timestamp.toFloat(), it.calories.toFloat()) }
        val dataSet = LineDataSet(entries, "Calories")
        dataSet.color = ContextCompat.getColor(this, android.R.color.holo_green_light)
        dataSet.setCircleColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(false)

        caloriesChart.data = LineData(dataSet)
        caloriesChart.invalidate()
    }

    private fun exportToCSV() {
        lifecycleScope.launch {
            try {
                val records = database.healthDao().getAllRecords()
                if (records.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No records to export", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val csvHeader = "ID,Timestamp,Steps,HeartRate,Calories\n"
                val csvData = StringBuilder(csvHeader)
                records.forEach { record ->
                    csvData.append("${record.id},${record.timestamp},${record.steps},${record.heartRate},${record.calories}\n")
                }

                val filename = "health_data_${System.currentTimeMillis()}.csv"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val resolver = contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    uri?.let {
                        resolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(csvData.toString().toByteArray())
                        }
                        Toast.makeText(this@MainActivity, "Exported to Downloads: $filename", Toast.LENGTH_LONG).show()
                    } ?: throw Exception("Failed to create MediaStore entry")
                } else {
                    // Fallback for older versions if needed, but minSDK is 26
                    Toast.makeText(this@MainActivity, "Export not supported on this version", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
