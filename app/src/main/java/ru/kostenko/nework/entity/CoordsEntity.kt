package ru.kostenko.nework.entity


import ru.kostenko.nework.dto.Coords

data class CoordsEntity(
    val coordslat: Double = 0.0,
    val coordslong: Double = 0.0,
) {
    fun toDto() = Coords(coordslat, coordslong)

    companion object {
        fun fromDto(dto: Coords?): CoordsEntity? {
            return if (dto != null) CoordsEntity(dto.latC, dto.longC) else null
        }
    }
}