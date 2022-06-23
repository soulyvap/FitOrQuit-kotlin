package com.example.fitorquit

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class ParticipantsAdapter(var participants: List<User>):
    RecyclerView.Adapter<ParticipantsAdapter.ViewHolder>() {

    val TAG = "ParticipantsAdapter"
    var onCancelClick: ((User) -> Unit)? = null
    var onAdminCheck: ((User, Boolean) -> Unit)? = null
    val repo = FirebaseRepo

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.participant_row_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = participants[position]
        repo.getPic("images/profile/${user.uid}.jpg")
            .downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(MyApplication.appContext)
                    .load(uri)
                    .placeholder(MyApplication.appContext.getDrawable(R.drawable.ic_launcher_foreground))
                    .into(holder.profile)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "getPic: ", e)
                holder.profile.background = MyApplication.appContext.getDrawable(R.color.light)
                Glide.with(MyApplication.appContext)
                    .load(MyApplication.appContext.getDrawable(R.drawable.ic_baseline_person_24))
                    .into(holder.profile)
            }

        if (position == 0) {
            holder.cancel.visibility = View.INVISIBLE
            holder.check.apply {
                isChecked = true
                isClickable = false
            }
        }

        holder.username.text = user.username

        if (position != 0) {
            holder.cancel.setOnClickListener {
                onCancelClick?.invoke(participants[position])
            }
            holder.check.setOnClickListener {
                val checkbox = it as CheckBox
                onAdminCheck?.invoke(participants[position], checkbox.isChecked)
            }
        }

    }

    override fun getItemCount() = participants.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateParticipants(newParts: List<User>) {
        participants = newParts
        notifyDataSetChanged()
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val check: CheckBox = view.findViewById(R.id.checkBox_participantsAdmin)
        val profile: ShapeableImageView = view.findViewById(R.id.img_creatorProfile)
        val username: TextView = view.findViewById(R.id.txt_participantUsername)
        val cancel: ImageView = view.findViewById(R.id.btn_participantsCancel)
    }

}