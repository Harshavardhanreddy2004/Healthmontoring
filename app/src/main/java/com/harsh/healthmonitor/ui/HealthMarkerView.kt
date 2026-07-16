package com.harsh.healthmonitor.ui

import android.content.Context
import android.widget.TextView
import com.harsh.healthmonitor.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HealthMarkerView(context: Context, private val baseTimestamp: Long) : MarkerView(context, R.layout.layout_chart_marker) {

    private val valueText: TextView = findViewById(R.id.marker_value)
    private val dateText: TextView = findViewById(R.id.marker_date)
    private val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")

    override fun refreshContent(e: Entry, highlight: Highlight) {
        val actualTimestamp = baseTimestamp + e.x.toLong()
        val date = Instant.ofEpochMilli(actualTimestamp)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

        valueText.text = e.y.toString()
        dateText.text = date

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}
