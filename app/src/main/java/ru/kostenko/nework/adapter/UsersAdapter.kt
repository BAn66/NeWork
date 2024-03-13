package ru.kostenko.nework.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.CardUserBinding
import ru.kostenko.nework.dto.User
import kotlin.properties.Delegates

interface OnUsersInteractionListener {
    fun onUserClicked(user: User)
    fun onUserCheckBoxClicked(user: User)
    fun onUserUnCheckBoxClicked(user: User)
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUsersInteractionListener: OnUsersInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.apply {
            userName.text = user.name
            userLogin.text = user.login

            if (userName.text.length < 1) {
                Glide.with(avatar)
                    .load(R.drawable.post_avatar_drawable)
                    .error(R.drawable.post_avatar_drawable)
                    .timeout(10_000)
                    .circleCrop()
                    .into(avatar)
            } else {
                val drawable = TextIconDrawable().apply {
                    text = user.name.substring(0, 1).uppercase()
                    textColor = Color.WHITE
                }
                Glide.with(avatar)
                    .load(user.avatar)
                    .error(drawable)
                    .timeout(10_000)
                    .circleCrop()
                    .into(avatar)
            }

            cardUser.setOnClickListener {
                onUsersInteractionListener.onUserClicked(user)
            }

            setPeople.isVisible = user.isTaken

            setPeople.setOnCheckedChangeListener(null)
            setPeople.isChecked = user.isChecked

            setPeople.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) onUsersInteractionListener.onUserCheckBoxClicked(user)
                else onUsersInteractionListener.onUserUnCheckBoxClicked(user)
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

class TextIconDrawable : Drawable() {
    private var alpha = 255
    private var textPaint = TextPaint().apply {
        textAlign = Paint.Align.CENTER
    }
    var text by Delegates.observable("") { _, _, _ -> invalidateSelf() }
    var textColor by Delegates.observable(Color.WHITE) { _, _, _ -> invalidateSelf() }

    private fun fitText(width: Int) {
        textPaint.textSize = 16f
        val widthAt16 = textPaint.measureText(text)
        textPaint.textSize = 16f / widthAt16 * (width /4F)
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()

        fitText(width)
        textPaint.color = ColorUtils.setAlphaComponent(textColor, alpha)
        val color = Color.parseColor("#6750A4")
        canvas.drawColor(color)
        canvas.drawText(text, width / 2f, height / 1.5f, textPaint)
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        textPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

}