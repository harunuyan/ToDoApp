package com.example.todoapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.databinding.RowLayoutBinding
import com.example.todoapp.model.Priority
import com.example.todoapp.model.ToDoData
import com.example.todoapp.view.fragments.list.ListFragmentDirections

class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    val dataList = ArrayList<ToDoData>()

    inner class ViewHolder(val binding: RowLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.titleTxt.text = dataList[position].title
        holder.binding.descriptionTxt.text = dataList[position].description

        when (dataList[position].priority) {
            Priority.HIGH -> holder.binding.priorityIndicator.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.red
                )
            )
            Priority.MEDIUM -> holder.binding.priorityIndicator.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.yellow
                )
            )
            Priority.LOW -> holder.binding.priorityIndicator.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.green
                )
            )
        }
        holder.binding.rowBackgrond.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(dataList[position])
            Navigation.findNavController(it).navigate(action)
        }
    }
    fun setData(toDoData: List<ToDoData>) {
        dataList.clear()
        dataList.addAll(toDoData)
        notifyDataSetChanged()
    }
}