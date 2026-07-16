package com.harsh.healthmonitor

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.harsh.healthmonitor.data.AppDatabase
import com.harsh.healthmonitor.data.HealthRecord
import com.harsh.healthmonitor.ui.HealthMarkerView
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
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ChartDetailActivity : AppCompatActivity() {

    private lateinit var titleText: TextView
    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var avgText: TextView
    private lateinit var maxText: TextView
    private lateinit var minText: TextView

    private val database by lazy { AppDatabase.getDatabase(this) }
    private var metricType: String? = null
    private var baseTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_detail)

        metricType = intent.getStringExtra("METRIC_TYPE")
        
        titleText = findViewById(R.id.detail_title)
        lineChart = findViewById(R.id.detail_line_chart)
        barChart = findViewById(R.id.detail_bar_chart)
        toggleGroup = findViewById(R.id.toggle_group)
        avgText = findViewById(R.id.summary_avg)
        maxText = findViewById(R.id.summary_max)
        minText = findViewById(R.id.summary_min)

        titleText.text = metricType ?: "Metric Detail"

        setupToggle()
        loadDataForPeriod(24 * 60 * 60 * 1000L) // Default 24 hours
    }

    private fun setupToggle() {
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val period = when (checkedId) {
                    R.id.btn_day -> 24 * 60 * 60 * 1000L
                    R.id.btn_week -> 7 * 24 * 60 * 60 * 1000L
                    R.id.btn_month -> 30 * 24 * 60 * 60 * 1000L
                    else -> 24 * 60 * 60 * 1000L
                }
                loadDataForPeriod(period)
            }
        }
    }

    private fun loadDataForPeriod(periodMs: Long) {
        val startTime = System.currentTimeMillis() - periodMs
        lifecycleScope.launch {
            val records = database.healthDao().getRecordsAfter(startTime)
            if (records.isNotEmpty()) {
                baseTimestamp = records.first().timestamp
                displayData(records)
                updateSummary(records)
            }
        }
    }

    private fun displayData(records: List<HealthRecord>) {
        if (metricType == "Steps") {
            lineChart.visibility = View.GONE
            barChart.visibility = View.VISIBLE
            setupChart(barChart)
            
            val entries = records.map { BarEntry((it.timestamp - baseTimestamp).toFloat(), it.steps.toFloat()) }
            val dataSet = BarDataSet(entries, "Steps")
            dataSet.color = ContextCompat.getColor(this, android.R.color.holo_blue_light)
            val data = BarData(dataSet)
            data.barWidth = 1800000f
            barChart.data = data
            barChart.animateX(1000)
        } else {
            barChart.visibility = View.GONE
            lineChart.visibility = View.VISIBLE
            setupChart(lineChart)
            
            val entries = records.map { 
                val y = when (metricType) {
                    "Heart Rate" -> it.heartRate.toFloat()
                    "Calories" -> it.calories.toFloat()
                    "Distance" -> it.distance.toFloat()
                    else -> 0f
                }
                Entry((it.timestamp - baseTimestamp).toFloat(), y) 
            }
            
            val label = metricType ?: "Value"
            val dataSet = LineDataSet(entries, label)
            styleLineChart(dataSet, getMetricColor())
            
            lineChart.data = LineData(dataSet)
            lineChart.animateX(1000)
        }
    }

    private fun setupChart(chart: com.github.mikephil.charting.charts.Chart<*>) {
        chart.description.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        chart.marker = HealthMarkerView(this, baseTimestamp)

        chart.xAxis.valueFormatter = object : ValueFormatter() {
            private val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
            override fun getFormattedValue(value: Float): String {
                return Instant.ofEpochMilli(baseTimestamp + value.toLong())
                    .atZone(ZoneId.systemDefault())
                    .format(formatter)
            }
        }
    }

    private fun styleLineChart(dataSet: LineDataSet, color: Int) {
        dataSet.color = color
        dataSet.setCircleColor(color)
        dataSet.lineWidth = 3f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawFilled(true)
        dataSet.fillColor = color
        dataSet.fillAlpha = 50
        dataSet.setDrawValues(false)
    }

    private fun getMetricColor(): Int {
        val resId = when (metricType) {
            "Heart Rate" -> android.R.color.holo_red_light
            "Calories" -> android.R.color.holo_green_light
            "Distance" -> android.R.color.holo_purple
            else -> android.R.color.holo_blue_light
        }
        return ContextCompat.getColor(this, resId)
    }

    private fun updateSummary(records: List<HealthRecord>) {
        val values = records.map { 
            when (metricType) {
                "Steps" -> it.steps.toDouble()
                "Heart Rate" -> it.heartRate
                "Calories" -> it.calories
                "Distance" -> it.distance
                else -> 0.0
            }
        }
        
        val avg = values.average()
        val max = values.maxOrNull() ?: 0.0
        val min = values.minOrNull() ?: 0.0

        avgText.text = String.format(Locale.US, "Avg: %.1f", avg)
        maxText.text = String.format(Locale.US, "Max: %.1f", max)
        minText.text = String.format(Locale.US, "Min: %.1f", min)
    }
}
