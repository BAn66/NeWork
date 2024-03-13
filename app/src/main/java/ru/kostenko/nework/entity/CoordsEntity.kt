package ru.kostenko.nework.entity


import ru.kostenko.nework.dto.Coords

data class CoordsEntity(
    val latitude: Double,
    val longitude: Double,
) {
    fun toDto() = Coords(latitude, longitude)

    companion object {
        fun fromDto(dto: Coords?): CoordsEntity? {
            return if (dto != null) CoordsEntity(dto.lat, dto.long) else null
        }
    }
}