package ru.kostenko.nework.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AndroidUtils {

    private val calendar = Calendar.getInstance()

    fun View.focusAndShowKeyboard() {
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
        requestFocus()
        if (hasWindowFocus()) {
            showTheKeyboardNow()
        } else {
            viewTreeObserver.addOnWindowFocusChangeListener(
                object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        if (hasFocus) {
                            this@focusAndShowKeyboard.showTheKeyboardNow()
                            viewTreeObserver.removeOnWindowFocusChangeListener(this)
                        }
                    }
                })
        }
    }

    fun eraseZero(number: Long): String {
        var s = ""

        when (number) {
            in 0..999 -> s = number.toString()
            in 1000..9999 -> s =
                (number.toDouble() / 1000).toString().substring(0, number.toString().length - 1)
                    .replace(".0", "") + "K"

            in 10000..999999 -> s =
                number.toString().substring(0, number.toString().length - 3) + "K"

            in 1000000..Int.MAX_VALUE -> s =
                (number.toDouble() / 1000000).toString().substring(0, number.toString().length - 4)
                    .replace(".0", "") + "M"
        }
        return s
    }

    fun pickDate(editText: EditText?, context: Context?) {
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = monthOfYear
            calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

            editText?.setText(
                SimpleDateFormat("MM/dd/yyyy", Locale.ROOT)
                    .format(calendar.time)
            )
        }
        DatePickerDialog(
            context!!,
            datePicker,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }
    fun pickTime(editText: EditText, context: Context) {
        val timePicker = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = minute

            editText.setText(
                SimpleDateFormat("HH:mm", Locale.ROOT)
                    .format(calendar.time)
            )
        }
        TimePickerDialog(
            context,
            timePicker,
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            true
        ).show()
    }

    fun formatDateForServer(value: String) : String {
        return if (value != "") {
            val dateTime = SimpleDateFormat(
                "MM/dd/yyyy HH:mm",
                Locale.getDefault()
            ).parse(value)
            val transform =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            transform.format(dateTime as Date)
        } else "1900-01-01T00:00:00Z"
    }

    fun pickDateForJob(editText: EditText?, context: Context?) {
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = monthOfYear
            calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

            editText?.setText(
                SimpleDateFormat("dd.MM.yyyy", Locale.ROOT)
                    .format(calendar.time)
            )
        }
        DatePickerDialog(
            context!!,
            datePicker,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    fun formatDateForJob(value: String) : String {
        return if (value != "") {
            val dateTime = SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
            ).parse(value)
            val transform =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            transform.format(dateTime as Date)
        } else "1900-01-01T00:00:00Z"
    }
}

