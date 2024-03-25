package ru.kostenko.nework.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.kostenko.nework.R

class AuthDialogFragmentFromUserDetails : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.want_like))
                .setMessage(getString(R.string.set_like_must_login))
                .setIcon(R.drawable.baseline_question_mark_24)
                .setPositiveButton(getString(R.string.login)) {
                        _, _ ->
                    findNavController().navigate(R.id.action_userDetailsFragment_to_authFragment)
                }
                .setNegativeButton(getString(R.string.registration)) {
                        _, _ ->
                    findNavController().navigate(R.id.action_userDetailsFragment_to_registrationFragment)
                }
                .setNeutralButton(getString(R.string.back)){
                        dialog, _ -> dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}