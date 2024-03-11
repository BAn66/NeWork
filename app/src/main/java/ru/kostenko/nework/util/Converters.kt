package ru.kostenko.nework.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.kostenko.nework.dto.UserPreview


class Converters {
    @TypeConverter
    fun fromSet(set: Set<Long>): String = set.joinToString("-")

    @TypeConverter
    fun toSet(data: String): Set<Long> =
        if (data.isBlank()) emptySet()
        else data.split("-").map { it.toLong() }.toSet()

    @TypeConverter
    fun fromString(value: String?): Map<Long, Pair<String, String>>? {
        val mapType = object : TypeToken<Map<Long, Pair<String, String>>?>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<Long, Pair<String, String>>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromStringtoUsers(value: String?): Map<Long, UserPreview>? {
        val mapType = object : TypeToken<Map<Long, UserPreview>?>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMapUsersToString(map: Map<Long, UserPreview>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }

}