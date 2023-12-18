package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var spinnerYear: Spinner
    private val overallMessageCountByMonth = mutableMapOf<String, Int>()
    private var selectedYear: String = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    private lateinit var spinnerChatType: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics_activity)

        barChart = findViewById(R.id.barChart)
        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerChatType = findViewById(R.id.spinnerChatType)
        setupChatTypeSpinner()
        setUpSpinnerWithYearsFromDatabase("chatRooms")
    }
    data class ChatType(val id: String, val displayName: String)

    private fun setupChatTypeSpinner() {
        val chatTypes = listOf(
            ChatType("chatRooms", "Random Chat"),
            ChatType("NearestChatRoom", "Nearest Chat")
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chatTypes.map { it.displayName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChatType.adapter = adapter

        // Thiết lập mặc định chọn "ChatRoom"
        val defaultSelection = chatTypes.indexOf(chatTypes[0])
        spinnerChatType.setSelection(defaultSelection)

        spinnerChatType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedChatType = chatTypes[position]
                val chatTypeId = selectedChatType.id
                fetchAndProcessMessages(chatTypeId)
                // Sử dụng chatTypeId cho mục tiếp theo của công việc của bạn
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }



    private fun fetchAndProcessMessages(chatType: String) {
        overallMessageCountByMonth.clear() // Xóa dữ liệu cũ
        val database = FirebaseDatabase.getInstance()
        val chatRoomsRef = database.getReference(chatType)

        chatRoomsRef.get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { chatRoomSnapshot ->
                chatRoomSnapshot.child("messages").children.forEach { messageSnapshot ->
                    val timestamp = messageSnapshot.child("timestamp").value as? Long ?: return@forEach
                    val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(timestamp))
                    overallMessageCountByMonth[monthYear] = overallMessageCountByMonth.getOrDefault(monthYear, 0) + 1
                }
            }
            // Lần đầu tiên tải dữ liệu, hiển thị dữ liệu cho năm hiện tại
            updateBarChartForYear(selectedYear,chatType)
        }
    }
    private fun setUpSpinnerWithYearsFromDatabase(chatType: String) {
        val uniqueYears = mutableSetOf<String>()

        val database = FirebaseDatabase.getInstance()
        // Sử dụng chatType để tham chiếu đến nút cụ thể
        val messagesRef = database.getReference(chatType)

        messagesRef.get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { messageSnapshot ->
                messageSnapshot.child("messages").children.forEach { messageSnapshot ->
                    val timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java)
                    timestamp?.let {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it
                        val year = calendar.get(Calendar.YEAR).toString()
                        uniqueYears.add(year)
                    }
                }
            }
            val yearsArray = uniqueYears.sorted().toTypedArray()

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsArray)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerYear.adapter = adapter

            // Thiết lập một năm mặc định nếu có
            spinnerYear.setSelection(yearsArray.indexOf(selectedYear))

            spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    selectedYear = yearsArray[position]
                    updateBarChartForYear(selectedYear, chatType) // Cập nhật chart dựa trên chatType và năm
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }.addOnFailureListener {
            // Xử lý lỗi
        }
    }
    private fun updateBarChartForYear(year: String, chatType: String) {
        // Lọc dữ liệu cho năm đã chọn từ dữ liệu tổng thể dựa trên chatType
        val filteredData = overallMessageCountByMonth.filterKeys { it.startsWith(year) }
        displayBarChart(filteredData)
    }

    class IntegerValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            // Sử dụng toInt() để chuyển giá trị Float thành Int và sau đó chuyển nó thành String
            return value.toInt().toString()
        }
    }
    private fun displayBarChart(data: Map<String, Int>) {
        val entries = ArrayList<BarEntry>()
        for (i in 0 until 12) {
            val monthIndex = i // Tháng bắt đầu từ 0
            val yearMonth = String.format("%s-%02d", selectedYear, monthIndex + 1)
            val count = data[yearMonth] ?: 0
            entries.add(BarEntry(i.toFloat(), count.toFloat()))
        }

        val barDataSet = BarDataSet(entries, "Số lượng tin nhắn")
        barDataSet.valueTextSize = 15f // Tăng kích thước chữ cho giá trị trên cột

        barDataSet.valueFormatter = IntegerValueFormatter()

        // Cấu hình để các cột có kích thước bằng nhau
        val barWidth = 0.5f // Điều chỉnh kích thước cột
        val barData = BarData(barDataSet)
        barData.barWidth = barWidth

        // Áp dụng các thay đổi vào biểu đồ
        barChart.data = barData
        customizeChartAppearance()
        // Điều chỉnh animation
        barChart.animateY(1000)
    }


    private fun getMonthLabels(): ArrayList<String> {
        // Giả định định dạng là "MM-yyyy", chỉ cần lấy phần "MM"
        return arrayListOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    private fun customizeChartAppearance() {
        val monthLabels = getMonthLabels()
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthLabels)

        // Cấu hình chữ
        barChart.xAxis.textSize = 20f // Tăng kích thước chữ cho trục X
        barChart.axisLeft.textSize = 20f // Tăng kích thước chữ cho trục Y bên trái
        barChart.axisRight.textSize = 12f // Tăng kích thước chữ cho trục Y bên phải
        // Căn chỉnh khoảng cách cho trục X
        barChart.xAxis.axisMinimum = -0.5f // Thêm khoảng cách ở đầu trục
        barChart.xAxis.axisMaximum = 11.5f // Thêm khoảng cách ở cuối trục
        barChart.setExtraOffsets(5f, 0f, 5f, 10f)
        barChart.setScaleEnabled(true)
        barChart.setPinchZoom(false) // Bạn có thể vô hiệu hóa pinch zoom nếu muốn
        barChart.setVisibleXRangeMaximum(6f)
        // Cấu hình cho các nhãn
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)

        // Cấu hình khác
        barChart.axisLeft.setDrawGridLines(false) // Ẩn grid lines cho trục Y bên trái
        barChart.axisRight.isEnabled = false // Ẩn trục Y bên phải
        barChart.description.isEnabled = false // Ẩn mô tả của biểu đồ
        barChart.legend.isEnabled = false // Ẩn chú giải
        barChart.xAxis.setCenterAxisLabels(false)

        // Cho phép zoom và cuộn
        barChart.setPinchZoom(true)
        barChart.setScaleEnabled(true)
        barChart.setDragEnabled(true)

        // Điều chỉnh biểu đồ
        barChart.invalidate()
    }
}