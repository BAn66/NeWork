package ru.kostenko.nework.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Converters {
    @TypeConverter
    fun fromSet(set: Set<Int>): String = set.joinToString("-")

    @TypeConverter
    fun toSet(data: String): Set<Int> =
        if (data.isBlank()) emptySet()
        else data.split("-").map { it.toInt() }.toSet()

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
}