package ru.kostenko.nework.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.kostenko.nework.R

class AuthDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Do you want to add a post?")
                .setMessage("To add a post, you must be logged in.")
                .setIcon(R.drawable.baseline_question_mark_24)
                .setPositiveButton("Login") {
                        _, _ ->
                    findNavController().navigate(R.id.action_mainFragment_to_authFragment)
                }
                .setNegativeButton("Registration") {
                        _, _ ->
                   findNavController().navigate(R.id.action_mainFragment_to_registrationFragment)
                }
                .setNeutralButton("Back"){
                        dialog, _ -> dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}