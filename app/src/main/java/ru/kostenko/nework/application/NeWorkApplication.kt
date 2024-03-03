package ru.kostenko.nework.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import ru.kostenko.nework.BuildConfig

@HiltAndroidApp
class NeWorkApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}