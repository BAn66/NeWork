package ru.kostenko.nework.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.CardJobBinding
import ru.kostenko.nework.dto.Job

interface OnJobInteractionListener {
    fun edit(job: Job)
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
        //TODO Разберись с датами
        binding.apply {
            jobName.text = job.name
            officePosition.text = job.position
            startDate.text = job.start?.substring(0,10)
            if (job.finish != null && job.finish != "1900-01-01T00:00:00Z") {
                endDate.text = job.finish.substring(0,10)
            } else {
                endDate.text = context.getString(R.string.until_now)
            }
            if (job.link != null && job.link != "hide") {
                webUrl.text = job.link
            } else {
                webUrl.visibility = View.GONE
            }

//            buttonMenuCardJob.isVisible = job.ownedByMe
//
//            buttonMenuCardJob.setOnClickListener {
//                PopupMenu(context, it).apply {
//                    inflate(R.menu.edit_options)
////                    menu.setGroupVisible(R.id.options, job.ownedByMe)
//                    setOnMenuItemClickListener { item ->
//                        when (item.itemId) {
//                            R.id.edit -> {
//                                listener.edit(job)
//                                true
//                            }
//
//                            R.id.remove -> {
//                                listener.delete(job)
//                                true
//                            }
//
//                            else -> false
//
//                        }
//                    }
//                }.show()
//            }
        }
    }
}


class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean =
        oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean = oldItem == newItem

}