package com.mianboor.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mianboor.app.databinding.ItemConfigBinding

class ConfigAdapter(
    private val items: List<ConfigItem>,
    private val onCopy: (ConfigItem) -> Unit,
    private val onShare: (ConfigItem) -> Unit,
    private val onConnect: (ConfigItem) -> Unit
) : RecyclerView.Adapter<ConfigAdapter.VH>() {

    class VH(val b: ItemConfigBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        with(holder.b) {
            tvName.text = item.name
            tvProto.text = item.protocol
            btnConnect.setOnClickListener { onConnect(item) }
            btnCopy.setOnClickListener { onCopy(item) }
            btnShare.setOnClickListener { onShare(item) }
        }
    }
}
