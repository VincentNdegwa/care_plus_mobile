package com.example.careplus.ui.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.careplus.R
import com.example.careplus.data.model.report.MissedMedication

class MissedMedicationAdapter(private val missedMedications: List<MissedMedication>) : RecyclerView.Adapter<MissedMedicationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val medicationNameTextView: TextView = view.findViewById(R.id.medicationNameTextView)
        val missedCountTextView: TextView = view.findViewById(R.id.missedCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_missed_medication, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val missedMedication = missedMedications[position]
        holder.medicationNameTextView.text = missedMedication.medication_name
        holder.missedCountTextView.text = "Missed Count: ${missedMedication.missed_count}"
    }

    override fun getItemCount(): Int = missedMedications.size
} 