package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.TabsAdapter
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentUserDetailsBinding
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.JobsViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import ru.kostenko.nework.viewmodel.WallViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailsFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val userViewModel: UserViewModel by activityViewModels()
    private val wallViewModel: WallViewModel by activityViewModels()
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        wallViewModel.clearWall()
        jobsViewModel.clearJobs()
        //Навигация через bottomMenu
//        val navController = requireNotNull(
//            childFragmentManager.findFragmentById(R.id.nav_user_fragment_main)
//        ).findNavController()
//        binding.navUserView.setupWithNavController(navController)

        //Навигация через Tabs
//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                // Handle tab select
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//                // Handle tab reselect
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//                // Handle tab unselect
//            }
//        })

        //TODO Сделай скрытие аватарки при скролле
        val tabLayout = binding.navUserTabs
        val viewPager = binding.navUserViewP2

        viewPager.adapter = TabsAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Wall"

                }
                1 -> {
                    tab.text = "Jobs"

                }
            }
        }.attach()
        //Данные о пользователе из userViewModel
        val user = userViewModel.user
        val nameUser = user.value?.name
        val loginUser = user.value?.login

        //Наполняем верхний аппбар
        toolbar = binding.toolbar
        toolbar.apply {
            setTitle("$nameUser / $loginUser")
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            if (user.value!!.id == authViewModel.data.value.id.toInt()) {
                inflateMenu(R.menu.exit_menu)
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.logout -> {
                        appAuth.removeAuth()
                        findNavController().popBackStack()
                        true
                    }

                    else -> false
                }
            }
        }

        //Установка аватарки в качестве фото профиля
        Glide.with(binding.userPhoto)
            .load(user.value?.avatar)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.video_holder)
            .timeout(10_000)
            .into(binding.userPhoto)

        return binding.root
    }

    fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WallFragment()
            1 -> JobsFragment()
            else -> WallFragment()
        }
    }
}