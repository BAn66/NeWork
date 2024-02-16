package ru.kostenko.nework.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager

object AndroidUtils {
    fun hideKeyboard(view: View) { //скрываем клавиатуру
        val inputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun View.focusAndShowKeyboard() { //показываем клавиатуру при редактировании поста
        /**
         * This is to be called when the window already has focus.
         */
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
        requestFocus()
        if (hasWindowFocus()) {
            // No need to wait for the window to get focus.
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            viewTreeObserver.addOnWindowFocusChangeListener(
                object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        // This notification will arrive just before the InputMethodManager gets set up.
                        if (hasFocus) {
                            this@focusAndShowKeyboard.showTheKeyboardNow()
                            // It’s very important to remove this listener once we are done.
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
}

