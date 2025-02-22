package com.example.careplus.ui.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.R
import com.example.careplus.data.model.report.TopSideEffect
import com.google.android.material.chip.Chip

class SideEffectAdapter(private val sideEffects: List<TopSideEffect>) : RecyclerView.Adapter<SideEffectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sideEffectName: TextView = view.findViewById(R.id.sideEffectName)
        val severityText: Chip = view.findViewById(R.id.severityText)
        val severityIndicator: View = view.findViewById(R.id.severityIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report_side_effect, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sideEffect = sideEffects[position]
        holder.sideEffectName.text = sideEffect.side_effect

        holder.severityText.apply {
            text = "Severity: ${sideEffect.severity}"
            setChipBackgroundColorResource(when(sideEffect.severity.lowercase()) {
                "mild" -> R.color.success
                "moderate" -> R.color.warning
                "severe" -> R.color.error
                else -> R.color.primary
            })
        }

        // Set severity indicator color
        holder.severityIndicator.setBackgroundResource(when(sideEffect.severity.lowercase()) {
            "mild" -> R.color.success
            "moderate" -> R.color.warning
            "severe" -> R.color.error
            else -> R.color.primary
        })
    }

    override fun getItemCount(): Int = sideEffects.size
} 