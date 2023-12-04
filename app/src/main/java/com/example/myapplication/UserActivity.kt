package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityDetailBinding
import com.example.myapplication.databinding.ActivityUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserActivity: AppCompatActivity() {
    private lateinit var  binding: ActivityUserBinding
    private  lateinit var databaseReferences: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reportedByUid = intent.getStringExtra("reportedByUid")
        val reportedUserUid = intent.getStringExtra("reportedUserUid")

        if(reportedUserUid == null){
            readData(reportedByUid)
        }else{
            readData(reportedUserUid)
        }

    }

    private fun readData(id:String?){
        if(id!=null){
            databaseReferences = FirebaseDatabase.getInstance().getReference("users")
            if (id != null) {
                databaseReferences.child(id).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Lấy giá trị từ snapshot
                        val username = snapshot.child("username").value
                        val email = snapshot.child("email").value
                        val gender = snapshot.child("gender").value
                        val dob = snapshot.child("age").value
                        val point = snapshot.child("point").value

                        binding.txtEmail.text = email.toString()
                        binding.txtUsername.text = username.toString()
                        binding.txtDOB.text = dob.toString()
                        binding.txtGender.text = gender.toString()
                        binding.txtPoint.text = point.toString()


                    }
                }
            }
        }
    }

}