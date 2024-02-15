package ru.kostenko.nework.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.kostenko.nework.dto.UserPreview
import java.lang.reflect.Type


class Converters {
    @TypeConverter
    fun fromSet(set: Set<Int>): String = set.joinToString("-")

    @TypeConverter
    fun toSet(data: String): Set<Int> =
        if (data.isBlank()) emptySet()
        else data.split("-").map { it.toInt() }.toSet()

    @TypeConverter
    fun fromString(value: String?): ArrayList<UserPreview>? {
        val listType = object : TypeToken<ArrayList<UserPreview>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<UserPreview>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

//    @TypeConverter
//    fun toUserPreview(value: String?): UserPreview? {
//        val listType: Type? = object : TypeToken<ArrayList<String?>?>() {}.type
//        return Gson().fromJson(value, listType)
//    }
//
//    @TypeConverter
//    fun fromUserPreview(userPreview: UserPreview?): String? {
//        val gson = Gson()
//        return gson.toJson(userPreview)
//    }
}