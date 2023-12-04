package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.mindrot.jbcrypt.BCrypt


class LoginActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityLoginBinding
    private  lateinit var databaseReferences: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)





        binding.btnLogin.setOnClickListener{
            val username = binding.loginAdmin.text.toString()
            val password = binding.loginPassword.text.toString()
            databaseReferences = FirebaseDatabase.getInstance().getReference("admin")
            databaseReferences.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object :
                ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Người dùng có tồn tại trong database
                            for (adminSnapshot in dataSnapshot.children) {
                                // Lấy thông tin của admin từ adminSnapshot
                                val storedHashedPassword = adminSnapshot.child("password").value as String?

                                if (checkPassword(password,storedHashedPassword)) {
                                    // Mật khẩu đúng
                                    Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                } else {
                                    // Mật khẩu sai hoặc không tồn tại
                                    Toast.makeText(this@LoginActivity, "Wrong password!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Wrong password!", Toast.LENGTH_SHORT).show()

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@LoginActivity, "Wrong Authentication!", Toast.LENGTH_SHORT).show()
                    }
                })

        }
    }



    fun checkPassword(password: String?, hashedPassword: String?): Boolean {
        // Sử dụng hàm checkpw của jBCrypt để so sánh mật khẩu
        return BCrypt.checkpw(password, hashedPassword)
    }
}