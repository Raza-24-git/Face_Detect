package com.myattendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
class MenuAdapter(private val menuList: List<MenuItem>, private var isExpanded: Boolean) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var visibleItems = if (isExpanded) menuList.size else 6

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.menu_icon)
        val title: TextView = view.findViewById(R.id.menu_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuList[position]
        holder.title.text = item.name

        Glide.with(holder.itemView.context)
            .load(item.iconUrl)
            .into(holder.icon)
    }

    override fun getItemCount(): Int = visibleItems

    fun toggleExpand(expand: Boolean) {
        isExpanded = expand
        visibleItems = if (isExpanded) menuList.size else 6
        notifyDataSetChanged() // Notify adapter to refresh the view with updated items
    }
}
