package com.sweetbytes.couplelife.ui.screen.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

@Composable
fun PieChartView(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 40f
                setUsePercentValues(false)
                legend.isEnabled = false
                setTouchEnabled(false)
            }
        },
        update = { chart ->
            val entries = slices.map { PieEntry(it.amount.toFloat(), "") }
            val dataSet = PieDataSet(entries, "").apply {
                colors = slices.map { it.color }
                valueTextSize = 0f
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        },
        modifier = modifier
    )
}
