package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReportedMessagesAdapter (var ds:List<reportData>): RecyclerView.Adapter<ReportedMessagesAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSTT: TextView = itemView.findViewById(R.id.tvSTT)
        val messageTextView: TextView = itemView.findViewById(R.id.message)
        val status: TextView = itemView.findViewById(R.id.status)

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
        holder.status.text = "status: ${message.status}"

        when (message.status) {
            "doing" ->{
                holder.itemView.findViewById<LinearLayout>(R.id.layoutMessage).setBackgroundResource(R.drawable.doing_border)
                holder.itemView.findViewById<TextView>(R.id.tvSTT).setBackgroundResource(R.drawable.doing_border)
                holder.itemView.findViewById<TextView>(R.id.txtDetail).setTextColor(Color.parseColor("#06d6a0"))
                holder.itemView.findViewById<TextView>(R.id.status).setTextColor(Color.parseColor("#06d6a0"))
            }
            "restored" -> {
                holder.itemView.findViewById<LinearLayout>(R.id.layoutMessage).setBackgroundResource(R.drawable.restored_border)
                holder.itemView.findViewById<TextView>(R.id.tvSTT).setBackgroundResource(R.drawable.restored_border)
                holder.itemView.findViewById<TextView>(R.id.txtDetail).setTextColor(Color.parseColor("#ffd166"))
                holder.itemView.findViewById<TextView>(R.id.status).setTextColor(Color.parseColor("#ffd166"))

            }
            "denied" -> {
                holder.itemView.findViewById<LinearLayout>(R.id.layoutMessage).setBackgroundResource(R.drawable.denied_border)
                holder.itemView.findViewById<TextView>(R.id.tvSTT).setBackgroundResource(R.drawable.denied_border)
                holder.itemView.findViewById<TextView>(R.id.txtDetail).setTextColor(Color.parseColor("#c5c9c7"))
                holder.itemView.findViewById<TextView>(R.id.status).setTextColor(Color.parseColor("#c5c9c7"))

            }
            "reported" -> {
                holder.itemView.findViewById<LinearLayout>(R.id.layoutMessage).setBackgroundResource(R.drawable.reported_border)
                holder.itemView.findViewById<TextView>(R.id.tvSTT).setBackgroundResource(R.drawable.reported_border)
                holder.itemView.findViewById<TextView>(R.id.txtDetail).setTextColor(Color.parseColor("#ff6b6b"))
                holder.itemView.findViewById<TextView>(R.id.status).setTextColor(Color.parseColor("#ff6b6b"))

            }
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