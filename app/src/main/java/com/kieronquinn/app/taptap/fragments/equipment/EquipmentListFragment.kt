package com.kieronquinn.app.taptap.fragments.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.adapters.EquipmentAdapter
import com.kieronquinn.app.taptap.models.*
import com.kieronquinn.app.taptap.utils.dip

class EquipmentListFragment : Fragment() {

    private var toolbarListener: ((Boolean) -> Unit)? = null

    private var itemClickListener: ((EquipmentInternal) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_action_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        val equipments = getEquipmentsForCategory()
        val adapter = EquipmentAdapter(recyclerView.context, equipments, true){
            itemClickListener?.invoke(equipments[it.adapterPosition])
        }
        recyclerView.adapter = adapter
        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            toolbarListener?.invoke(recyclerView.computeVerticalScrollOffset() > 0)
        }
        recyclerView.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(v.paddingLeft, v.paddingTop, 0, insets.systemWindowInsetBottom + v.context.dip(8))
            insets
        }
    }

    private fun getEquipmentsForCategory(): MutableList<EquipmentInternal> {
        return TapEquipment.values().filter { it.isAvailable }.map { EquipmentInternal(it, ArrayList()) }.toMutableList()
    }
    fun getToolbarTitle(): String {
        return getString(R.string.fab_add_equipment)
    }

    fun setToolbarListener(listener: (Boolean) -> Unit){
        this.toolbarListener = listener
    }

    fun setItemClickListener(listener: (EquipmentInternal) -> Unit){
        this.itemClickListener = listener
    }

}