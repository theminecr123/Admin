package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityDetailBinding
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DetailActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityDetailBinding
    private  lateinit var databaseReferences: DatabaseReference
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val reportedMessagesList = mutableListOf<reportData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val roomID = intent.getStringExtra("roomID")
        val messageId = intent.getStringExtra("messageId")
        val reportedByUid = intent.getStringExtra("reportedByUid")
        val reportedUserUid = intent.getStringExtra("reportedUserUid")
        val message = intent.getStringExtra("message")
        val timestamp = intent.getLongExtra("timestamp",0)
        val status = intent.getStringExtra("status")

        when (status) {
            "doing" -> {
                binding.txtStatus.setTextColor(Color.GREEN)
                binding.txtStatus.text = "DOING"
            }
            "reported" -> {
                binding.txtStatus.setTextColor(Color.RED)
                binding.txtStatus.text = "REPORTED"
            }
            "denied"->{
                binding.txtStatus.setTextColor(Color.GRAY)
                binding.txtStatus.text = "DENIED"
                binding.btnConfirm.visibility = View.GONE
                binding.btnDeny.visibility =View.GONE
            }
            "restored"->{
                binding.txtStatus.setTextColor(Color.BLUE)
                binding.txtStatus.text = "RESTORED"
                binding.btnConfirm.visibility =View.GONE
                binding.btnDeny.visibility =View.GONE
            }
        }




        readData1(reportedByUid)
        readData2(reportedUserUid)

        binding.txtMessage.text = message
        binding.tvTime.text = dateFormat.format(Date(timestamp)).toString()

        binding.layoutUser1.setOnClickListener{
            val intent = Intent(this, UserActivity::class.java)
            intent.putExtra("reportedByUid", reportedByUid)
            startActivity(intent)
        }

        binding.layoutUser2.setOnClickListener{
            val intent = Intent(this, UserActivity::class.java)
            intent.putExtra("reportedUserUid", reportedUserUid)
            startActivity(intent)

        }

        binding.btnConfirm.setOnClickListener{
            if (roomID != null) {
                if (messageId != null) {
                    confirmReport(reportedUserUid,roomID,messageId)
                }
            }
        }
        binding.btnDeny.setOnClickListener{
            denyReport(roomID!!,messageId!!)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }





    }

    fun readData1(id: String?) {
        databaseReferences = FirebaseDatabase.getInstance().getReference("users")
        if (id != null) {
            databaseReferences.child(id).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Lấy giá trị từ snapshot
                    val username = snapshot.child("username").value
                    val email = snapshot.child("email").value

                    binding.txtEmail1.text = email.toString()
                    binding.txtUIDReport.text = username.toString()


                }
            }
        }
    }

    fun readData2(id: String?) {
        databaseReferences = FirebaseDatabase.getInstance().getReference("users")
        if (id != null) {
            databaseReferences.child(id).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Lấy giá trị từ snapshot
                    val username = snapshot.child("username").value
                    val email = snapshot.child("email").value

                    binding.txtEmail2.text = email.toString()
                    binding.txtUIDBeReported.text = username.toString()


                }
            }
        }
    }

    fun confirmReport(id: String?, roomID:String ,messageID: String) {
        if (id == null) {
            return
        }

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users")

        userRef.child(id).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val point = snapshot.child("point").value.toString().toInt()
                val remainingPoint = point - 10
                val restorePoint = point + 10

                val reportsRef = FirebaseDatabase.getInstance().getReference("reports")

                reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (roomSnapshot in dataSnapshot.children) {
                            for (messageSnapshot in roomSnapshot.children) {
                                val status = messageSnapshot.child("status").value as String?

                                if(status == "doing"){
                                    val reportRef = database.getReference("reports").child(roomID!!).orderByKey().equalTo(messageID)
                                    reportRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (snapshot in snapshot.children) {
                                                val reportRef = snapshot.ref
                                                val updatedData = HashMap<String, Any>()
                                                updatedData["status"] = "reported"
                                                reportRef.updateChildren(updatedData)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // Xử lý khi có lỗi xảy ra
                                        }
                                    })

                                    val pointRef = userRef.child(id).child("point")
                                    pointRef.setValue(remainingPoint)
                                    startActivity(Intent(this@DetailActivity,MainActivity::class.java))
                                } else if(status == "reported"){
                                    val reportRef = database.getReference("reports").child(roomID!!).orderByKey().equalTo(messageID)
                                    reportRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (snapshot in snapshot.children) {
                                                val reportRef = snapshot.ref
                                                val updatedData = HashMap<String, Any>()
                                                updatedData["status"] = "restored"
                                                reportRef.updateChildren(updatedData)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // Xử lý khi có lỗi xảy ra
                                        }
                                    })

                                    val pointRef = userRef.child(id).child("point")
                                    pointRef.setValue(restorePoint)
                                    startActivity(Intent(this@DetailActivity,HistoryActivity::class.java))
                                }
                            }
                        }
                        finish() // Kết thúc Activity hiện tại
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Xử lý khi có lỗi xảy ra
                    }
                })
            }
        }
    }

    fun denyReport(roomID: String, messageID: String) {
        val database = FirebaseDatabase.getInstance()
        val reportRef = database.getReference("reports").child(roomID).child(messageID)

        reportRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val status = snapshot.child("status").value as? String
                    if (status == "doing") {
                        // Trạng thái báo cáo là 'doing', tiến hành cập nhật thành 'denied'
                        reportRef.child("status").setValue("denied").addOnSuccessListener {
                            Toast.makeText(this@DetailActivity, "Report status updated to denied", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this@DetailActivity, "Failed to update report status", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        startActivity( Intent(this@DetailActivity,HistoryActivity::class.java))


                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Report does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailActivity, "Error accessing the database", Toast.LENGTH_SHORT).show()
            }
        })
    }



    override fun onBackPressed() {
        super.onBackPressed()
        finish()

    }
}