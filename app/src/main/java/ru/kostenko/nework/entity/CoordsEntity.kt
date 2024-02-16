package ru.kostenko.nework.entity

import androidx.room.Entity
import ru.kostenko.nework.dto.Coords

@Entity
data class CoordsEntity(
    val coordslat: Int = 0,
    val coordslong: Int = 0,
) {
    fun toDto() = Coords(coordslat, coordslong)

    companion object {
        fun fromDto(dto: Coords?): CoordsEntity? {
            return if (dto != null) CoordsEntity(dto.latC, dto.longC) else null
        }
    }
}