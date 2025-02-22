package com.example.careplus.ui.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.R
import com.example.careplus.data.model.report.TopSideEffect

class SideEffectAdapter(private val sideEffects: List<TopSideEffect>) : RecyclerView.Adapter<SideEffectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sideEffectName: TextView = view.findViewById(R.id.sideEffectName)
        val severityText: TextView = view.findViewById(R.id.severityText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_side_effect, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sideEffect = sideEffects[position]
        holder.sideEffectName.text = sideEffect.side_effect
        holder.severityText.text = "Severity: ${sideEffect.severity}"
    }

    override fun getItemCount(): Int = sideEffects.size
} 