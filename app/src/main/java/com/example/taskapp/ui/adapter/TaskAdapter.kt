package com.example.taskapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskapp.data.model.Task
import com.example.taskapp.databinding.ItemTaskBinding

class TaskAdapter(
    private val taskList: List<Task>
) : RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val task = taskList[position]

        holder.binding.textDescription.text = task.description
    }

    override fun getItemCount() = taskList.size

    inner class MyViewHolder(val binding: ItemTaskBinding)
        : RecyclerView.ViewHolder(binding.root) {

    }
}