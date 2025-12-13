package org.hak.fitnesstrackerapp.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T>(
    protected val items: List<T>,
    private val layoutResId: Int
) : RecyclerView.Adapter<BaseViewHolder<T>>() {

    abstract fun createViewHolder(parent: ViewGroup): BaseViewHolder<T>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        return createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

abstract class BaseViewHolder<T>(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T)
}