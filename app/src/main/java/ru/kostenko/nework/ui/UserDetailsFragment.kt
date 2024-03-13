package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
    @Inject
    lateinit var appAuth: AppAuth
    private val userViewModel: UserViewModel by activityViewModels()
    private val wallViewModel: WallViewModel by activityViewModels()
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        wallViewModel.clearWall()
        jobsViewModel.clearJobs()

        val tabLayout = binding.navUserTabs
        val viewPager = binding.navUserViewP2

        viewPager.adapter = TabsAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setText(R.string.wall)
                }
                1 -> {
                    tab.setText(R.string.jobs)
                }
            }
        }.attach()
        val user = userViewModel.user
        val nameUser = user.value?.name
        val loginUser = user.value?.login

        val toolbar: Toolbar = binding.toolbar
        toolbar.apply {
            title = "$nameUser / $loginUser"
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

        Glide.with(binding.userPhoto)
            .load(user.value?.avatar)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.video_holder)
            .timeout(10_000)
            .into(binding.userPhoto)

        return binding.root
    }
}