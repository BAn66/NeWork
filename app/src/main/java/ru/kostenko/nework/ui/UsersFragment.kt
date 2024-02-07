package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.kostenko.nework.databinding.FragmentUsersBinding


class UsersFragment: Fragment() {

    class EventsFragment: Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val binding = FragmentUsersBinding.inflate(layoutInflater)

            binding.addUser.setOnClickListener {
                Toast.makeText(this.context, "добавь событие", Toast.LENGTH_SHORT).show()
            }

            return binding.root
        }
    }
}