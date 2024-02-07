package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentEventsBinding

class EventsFragment: Fragment() {
    private lateinit var toolbar_events: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventsBinding.inflate(layoutInflater)

        /*Верхнее меню*/
        toolbar_events = binding.root.findViewById<Toolbar>(R.id.toolbar)
        toolbar_events.apply{
            setTitle(R.string.app_name)
            inflateMenu(R.menu.auth_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.authentication -> {
                        Toast.makeText(this.context, "авторизуйся", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_eventsFragment_to_authFragment)
                        true
                    }
                    else -> false
                }
            }
        }
        binding.addEvent.setOnClickListener {
            Toast.makeText(this.context, "добавь событие", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }
}