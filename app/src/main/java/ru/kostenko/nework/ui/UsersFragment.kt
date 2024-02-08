package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentUsersBinding


class UsersFragment: Fragment() {
    private lateinit var toolbar_users: Toolbar

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val binding = FragmentUsersBinding.inflate(layoutInflater)

            /*Верхнее меню*/
            toolbar_users = binding.toolbar
            toolbar_users.apply{
                setTitle(R.string.app_name)
                inflateMenu(R.menu.auth_menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.authentication -> {
                            Toast.makeText(this.context, "авторизуйся", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }
            }

            return binding.root
        }
    }
