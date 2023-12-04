package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportedMessagesAdapter (var ds:List<reportData>): RecyclerView.Adapter<ReportedMessagesAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSTT: TextView = itemView.findViewById(R.id.tvSTT)
        val messageTextView: TextView = itemView.findViewById(R.id.message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.report_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ds.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val message = ds[position]
        holder.tvSTT.text = "${position+1}"
        holder.messageTextView.text = "${message.message}"

        if (message.status == "restored") {
            holder.itemView.isEnabled = false
            holder.itemView.alpha = 0.5f // Đặt độ mờ cho itemView bị disable
        } else {
            holder.itemView.isEnabled = true
            holder.itemView.alpha = 1.0f // Đặt độ mờ cho itemView bình thường
        }


        holder.itemView.setOnClickListener {
            // Xử lý khi item được chọn
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("roomID",message.roomID)
            intent.putExtra("messageId", message.messaseID)
            intent.putExtra("reportedByUid", message.UID_report)
            intent.putExtra("reportedUserUid", message.UID_beReported)
            intent.putExtra("message", message.message)
            intent.putExtra("timestamp",message.timestamp)
            intent.putExtra("status",message.status)
            animation(holder.itemView)
            holder.itemView.context.startActivity(intent)
        }
    }




     private fun animation(view:View) {
        val animation: Animation = AnimationUtils.loadAnimation(view.context ,android.R.anim.slide_in_left)
        view.animation = animation

    }
}