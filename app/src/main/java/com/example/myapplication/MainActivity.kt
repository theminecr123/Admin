package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityDetailBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private var backPressCount = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportedMessagesAdapter
    private val reportedMessagesList = mutableListOf<reportData>()
    private lateinit var databaseReference: DatabaseReference
    val layoutManager = GridLayoutManager(this, 2)
    private lateinit var  binding: ActivityMainBinding
    companion object {
        const val DETAIL_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        adapter = ReportedMessagesAdapter(reportedMessagesList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        binding.btnHistory.setOnClickListener{
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firebase Realtime Database reference
        val reportsRef = FirebaseDatabase.getInstance().getReference("reports")

        reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                reportedMessagesList.clear()
                for (roomSnapshot in dataSnapshot.children) {
                    val roomID = roomSnapshot.key
                    for (messageSnapshot in roomSnapshot.children) {
                        val messageID = messageSnapshot.key
                        val reportedByUid = messageSnapshot.child("uid_report").value as String?
                        val reportedUserUid = messageSnapshot.child("uid_beReported").value as String?
                        val status = messageSnapshot.child("status").value as String?

                        val messageMapSnapshot = messageSnapshot.child("messageMap")
                        val messageMap = messageMapSnapshot.getValue() as Map<String, Any?>
                        // Sử dụng dữ liệu messageMap ở đây
                        val timestamp = messageMap["timestamp"] as Long
                        val content = messageMap["content"] as String

                        if(status=="doing"){
                            val reportedMessage = reportData(roomID,messageID, reportedByUid, reportedUserUid, content, timestamp,status)
                            reportedMessagesList.add(reportedMessage)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        })

    }


    override fun onBackPressed() {
        if (backPressCount < 4) {
            backPressCount++
            Toast.makeText(this@MainActivity, "Press ${5-backPressCount} times to Back to login!", Toast.LENGTH_SHORT).show()
        } else {
            // If five consecutive presses, you can finish the activity or handle as needed.
            super.onBackPressed()

        }
    }
}