package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.kostenko.nework.databinding.FragmentEventsBinding

class EventsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventsBinding.inflate(layoutInflater)

        binding.addEvent.setOnClickListener {
            Toast.makeText(this.context, "добавь событие", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }
}