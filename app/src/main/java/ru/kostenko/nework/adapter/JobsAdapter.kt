package ru.kostenko.nework.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.CardJobBinding
import ru.kostenko.nework.dto.Job
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface OnJobInteractionListener {
    fun delete(job: Job)
}

class JobsAdapter(private val listener: OnJobInteractionListener) :
    ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(
            binding,
            listener,
            parent.context
        )
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val listener: OnJobInteractionListener,
    private val context: Context,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {
        binding.apply {
            jobName.text = job.name
            officePosition.text = job.position

            startDate.text = OffsetDateTime.parse(job.start)
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy - "))
            if (job.finish != null && job.finish != "1900-01-01T00:00:00Z"
            ) {
                endDate.text = OffsetDateTime.parse(job.finish)
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            } else {
                endDate.text = context.getString(R.string.until_now)
            }
            if (job.link != null
                && job.link != ""
            ) {
                webUrl.text = job.link
            } else {
                webUrl.visibility = View.GONE
            }

            removeJob.isVisible = job.ownedByMe
            removeJob.setOnClickListener { listener.delete(job) }
        }
    }
}


class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean = oldItem == newItem

}