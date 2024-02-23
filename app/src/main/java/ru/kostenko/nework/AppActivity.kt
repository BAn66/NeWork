package ru.kostenko.nework


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.authorization.AppAuth

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app)
//{
//    private lateinit var binding: ActivityMainBinding
////    private lateinit var navHostFragment: NavHostFragment
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
////        navHostFragment =
////            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
////        val navController = navHostFragment.navController
//
////        if (savedInstanceState == null) {
////            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.postsFragment)
////        }
//
//        val navView: BottomNavigationView = binding.navView
//        navView.setupWithNavController(findNavController(R.id.nav_host_fragment_activity_main))
//        binding.navView.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.navigation_posts -> {
//                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.postsFragment)
////                    navController.navigate(R.id.postsFragment)
////                    showFragment("PostsFragment")
//                    true
//                }
//
//                R.id.navigation_events -> {
//                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.eventsFragment)
////                    navController.navigate(R.id.eventsFragment)
////                    showFragment("EventsFragment")
//                    true
//                }
//
//                R.id.navigation_users -> {
//                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.usersFragment)
////                    navController.navigate(R.id.usersFragment)
////                    showFragment("UsersFragment")
//                    true
//                }
//
//                else -> false
//            }
//        }
//    }


//    fun showFragment(fragmentName: String) {
//        val fragmentManager: FragmentManager = supportFragmentManager
//        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
//
//        // Получаем список фрагментов, которые находятся в FragmentManager
//        val existingFragments = fragmentManager.getFragments() as ArrayList<Fragment>
//
//        // Фрагмент, который в данный момент отображен на экране
//        var shownFragment: Fragment? = null
////        if (existingFragments != null) {
//            for (curFragment in existingFragments) {
//                if (curFragment.isVisible) {
//                    shownFragment = curFragment
//                    break
//                }
//            }
////        }
//
//        // Фрагмент, который необходимо отобразить на экране
//        var neededFragment: Fragment? = null
//
//        // Если в данный момент на экране не отображен ни один фрагмент, либо отображен, но не тот, который требуется показать
//        if (shownFragment == null || shownFragment.javaClass.simpleName != fragmentName) {
//
//            // Проверяем, есть ли фрагмент, который нужно отобразить, в FragmentManager
//            if (shownFragment != null) neededFragment =
//                fragmentManager.findFragmentByTag(fragmentName)
//
//            // Если нужного фрагмента нет, то создаем его и добавляем в FragmentManager
//            if (neededFragment == null) {
//                when (fragmentName) {
//                    "PostsFragment" -> {
//                        neededFragment = PostsFragment()
//                        fragmentTransaction.add(
//                            R.id.container,
//                            neededFragment,
//                            "FragmentOne"
//                        )
//                    }
//
//                    "EventsFragment" -> {
//                        neededFragment = EventsFragment()
//                        fragmentTransaction.add(
//                            R.id.container,
//                            neededFragment,
//                            "FragmentTwo"
//                        )
//                    }
//
//                    "UsersFragment" -> {
//                        neededFragment = UsersFragment()
//                        fragmentTransaction.add(
//                            R.id.container,
//                            neededFragment,
//                            "FragmentThree"
//                        )
//                    }
//                }
//            }
//
//            // Скрываем старый фрагмент
//            if (shownFragment != null) fragmentTransaction.hide(shownFragment)
//
//            // Показываем новый фрагмент
//            if (neededFragment != null) {
//                fragmentTransaction.show(neededFragment)
//            }
//            fragmentTransaction.commit()
//        }
//    }

