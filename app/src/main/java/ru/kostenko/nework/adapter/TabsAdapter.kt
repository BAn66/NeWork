package ru.kostenko.nework.adapter

import androidx.fragment.app.Fragment

import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.kostenko.nework.ui.JobsFragment
import ru.kostenko.nework.ui.WallFragment

class TabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> WallFragment()
            1-> JobsFragment()
            else -> WallFragment()
        }
    }


}