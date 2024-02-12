package ru.kostenko.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.kostenko.nework.databinding.CardUserBinding
import ru.kostenko.nework.dto.User


interface OnUsersInteractionListener {
    fun onOpenProfile(user: User)
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUsersInteractionListener: OnUsersInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.apply {
            userName.text = user.name
            userLogin.text = user.login
            Glide.with(avatar)
                .load(user.avatar)
                .error(userName.text.substring(0, 1).uppercase())
                .timeout(10_000)
                .circleCrop()
                .into(avatar)

            avatar.setOnClickListener {
                onUsersInteractionListener.onOpenProfile(user)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}

class UsersAdapter(
    private val onUserListInteractionListener: OnUsersInteractionListener
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onUserListInteractionListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }
}