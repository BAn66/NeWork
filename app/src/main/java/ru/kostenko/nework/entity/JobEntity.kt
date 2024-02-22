package ru.kostenko.nework.entity;

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kostenko.nework.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val position: String,
    val start: String? = null,
    val finish: String? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
) {
    fun toDto() = Job(
        id = id,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link,
        ownedByMe = ownedByMe
    )

    companion object {
        fun fromDto(dto: Job) = JobEntity(
            id = dto.id,
            name = dto.name,
            position = dto.position,
            start = dto.start,
            finish = dto.finish,
            link = dto.link,
            ownedByMe = dto.ownedByMe,
        )
    }

}

fun List<JobEntity>.toDto() = map(JobEntity::toDto)
fun List<Job>.toEntity() = map(JobEntity::fromDto)