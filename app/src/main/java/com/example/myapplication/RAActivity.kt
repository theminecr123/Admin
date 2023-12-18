package com.example.myapplication

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RAActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_raactivity)
        setupPieChart()

    }
    private fun setupPieChart() {
        pieChart = findViewById(R.id.pieChart)

        // Cấu hình biểu đồ tròn
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(android.R.color.white)
        pieChart.transparentCircleRadius = 61f

        // Lấy dữ liệu trạng thái từ Firebase Realtime Database và tạo dữ liệu cho biểu đồ tròn
        val entries = ArrayList<PieEntry>()
        val database = FirebaseDatabase.getInstance()
        val chatRoomsRef = database.getReference("reports")

        chatRoomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var statusCountMap = mutableMapOf<String, Int>()

                    for (chatRoomSnapshot in dataSnapshot.children) {
                        for (messageSnapshot in chatRoomSnapshot.children) {
                            val status = messageSnapshot.child("status").getValue(String::class.java)
                            if (status != null) {
                                statusCountMap[status] = statusCountMap.getOrDefault(status, 0) + 1
                            }
                        }
                    }

                    for ((status, count) in statusCountMap) {
                        entries.add(PieEntry(count.toFloat(), status))
                    }

                    // Tạo dữ liệu cho biểu đồ tròn
                    val dataSet = PieDataSet(entries, "")
                    dataSet.sliceSpace = 3f
                    dataSet.selectionShift = 5f

                    val colors = ArrayList<Int>()
                    colors.add(Color.parseColor("#06d6a0")) // bright green
                    colors.add(Color.parseColor("#ff6b6b")) // bright red
                    colors.add(Color.parseColor("#ffd166")) // bright yellow
                    colors.add(Color.parseColor("#c5c9c7")) // pastel grey

                    dataSet.colors = colors

                    val pieData = PieData(dataSet)
                    pieData.setValueTextSize(20f)
                    pieData.setValueTextColor(Color.BLACK)
                    pieChart.data = pieData

                    // Hiển thị legen và cấu hình vị trí
                    val legend = pieChart.legend
                    legend.textSize = 15f // Thay đổi giá trị tùy theo kích thước mong muốn

                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    legend.orientation = Legend.LegendOrientation.HORIZONTAL
                    legend.formSize = 20f // Đặt kích thước cho hình biểu diễn trong legend
                    legend.xEntrySpace = 30f // Đặt khoảng cách ngang giữa các mục
                    legend.yEntrySpace = 20f // Đặt khoảng cách dọc giữa các mục
                    legend.yOffset = 20f // Đặt khoảng cách từ dưới cùng của biểu đồ
                    legend.setDrawInside(false)
                    legend.isEnabled = true
                    pieChart.invalidate() // Để cập nhật biểu đồ với các thay đổi
                    pieChart.animateXY(1400, 1400)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi nếu có
            }
        })
    }
    class MyValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString() // Chỉ trả về số lượng
        }
    }
}