package com.kieronquinn.app.taptap.fragments

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.adapters.EquipmentAdapter
import com.kieronquinn.app.taptap.fragments.bottomsheets.EquipmentBottomSheetFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.GenericBottomSheetFragment
import com.kieronquinn.app.taptap.models.EquipmentInternal
import com.kieronquinn.app.taptap.models.store.EquipmentListFile
import com.kieronquinn.app.taptap.utils.*
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_settings_calibration.*
import kotlinx.android.synthetic.main.item_action.view.*
import java.lang.RuntimeException

class SettingsCalibrationFragment : BaseFragment() {

    companion object {
        const val addResultKey = "ADD_EQUIPMENT_RESULT"
        const val PREF_KEY_EQUIPMENT_HELP_SHOWN = "equipment_help_shown"
    }

    private val recyclerView by lazy {
        recycler_view
    }

    private val fab by lazy {
        fab_equipment
    }

    private val animationAddToDelete by lazy {
        context?.let {
            if (!isAdded) null
            else ContextCompat.getDrawable(it, R.drawable.ic_add_to_delete) as AnimatedVectorDrawable
        }
    }

    private val animationDeleteToAdd by lazy {
        context?.let {
            if (!isAdded) null
            else ContextCompat.getDrawable(it, R.drawable.ic_delete_to_add) as AnimatedVectorDrawable
        }
    }

    private val equipments by lazy {
        EquipmentListFile.loadFromFile(requireContext()).mapNotNull {
            try {
                if(it.action == null) null
                else it
            } catch (e: RuntimeException) {
                null
            }
        }.toMutableList()
    }

    private val itemTouchHelper by lazy {

        var isFabDrop = false

        var draggingItem: EquipmentInternal? = null
        var draggingViewHolder: RecyclerView.ViewHolder? = null

        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    val adapter = recyclerView.adapter as EquipmentAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    adapter.moveItem(from, to)
                    adapter.notifyItemMoved(from, to)
                    saveToFile()
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                    super.onSelectedChanged(viewHolder, actionState)
                    if (actionState == ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.run {
                            draggingItem = equipments[viewHolder.adapterPosition]
                            draggingViewHolder = viewHolder
                            fakeCard.cloneSize(this)
                            viewHolder.itemView.visibility = View.INVISIBLE
                            fakeCard.visibility = View.VISIBLE
                            fakeCard.item_action_name.text = item_action_name.text
                            fakeCard.item_action_icon.setImageDrawable(item_action_icon.drawable)
                            fakeCard.item_action_description.text = item_action_description.text
                        }
                        setFabState(true)
                    }
                    if (actionState == ACTION_STATE_IDLE && fakeCard.isOverlapping(fab)) {
                        val action = draggingItem ?: return
                        val draggedViewHolder = draggingViewHolder ?: return
                        val position = recyclerView.layoutManager?.getPosition(draggedViewHolder.itemView)
                            ?: -1
                        equipments.remove(action)
                        recyclerView.adapter?.notifyItemRemoved(position)
                        //Fix for item hanging around after removal
                        draggedViewHolder.itemView.run {
                            (parent as ViewGroup).removeView(this)
                        }
                        saveToFile()
                    }
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.visibility = View.VISIBLE
                    fakeCard.visibility = View.GONE
                    setFabState(false)
                }

                override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                    viewHolder?.itemView?.run {
                        fakeCard.clonePosition(this)
                        if (fakeCard.isOverlapping(fab)) {
                            if (!isFabDrop) {
                                fakeCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(fab.context, R.color.fab_color_delete))
                                fab.text = getString(R.string.fab_remove_action_drop)
                                TransitionManager.beginDelayedTransition(fab.parent as ViewGroup)
                                isFabDrop = true
                            }
                        } else {
                            if (isFabDrop) {
                                fakeCard.backgroundTintList = null
                                fab.text = getString(R.string.fab_remove_action)
                                TransitionManager.beginDelayedTransition(fab.parent as ViewGroup)
                                isFabDrop = false
                            }
                        }
                    }
                    super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    private fun setFabState(removeEnabled: Boolean) {
        if (!isAdded) return
        val colorRemove = ContextCompat.getColor(fab.context, R.color.fab_color_delete)
        val colorAdd = ContextCompat.getColor(fab.context, R.color.fab_color)
        if (removeEnabled) {
            fab.text = fab.context.getString(R.string.fab_remove_action)
            fab.icon = animationAddToDelete
            animationAddToDelete?.start()
            fab.animateBackgroundStateChange(colorAdd, colorRemove)
            fab.isClickable = false
            fab.isFocusable = false
        } else {
            fab.text = fab.context.getString(R.string.fab_add_equipment)
            fab.icon = animationDeleteToAdd
            animationDeleteToAdd?.start()
            fab.animateBackgroundStateChange(colorRemove, colorAdd)
            fab.isClickable = true
            fab.isFocusable = true
        }
        TransitionManager.beginDelayedTransition(fab.parent as ViewGroup)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings_calibration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = EquipmentAdapter(recyclerView.context, equipments) {
            itemTouchHelper.startDrag(it)
        }
        fab.applySystemWindowInsetsToMargin(bottom = true)
        fab.post {
            setupRecyclerView(recyclerView, extraBottomPadding = fab.height + fab.marginBottom)
        }
        fab.setOnClickListener {
            showActionBottomSheet()
        }
        itemTouchHelper.attachToRecyclerView(recyclerView)
        setFragmentResultListener(addResultKey) { key, bundle ->
            val newItem = bundle.get(addResultKey) as EquipmentInternal
            equipments.add(newItem)
            recyclerView.adapter?.notifyItemInserted(equipments.size)
            recyclerView?.layoutManager?.scrollToPosition(equipments.size - 1)
            saveToFile()
        }
        if (sharedPreferences?.getBoolean(PREF_KEY_EQUIPMENT_HELP_SHOWN, false) == false) {
            showHelpBottomSheet()
        }
    }

    private fun showActionBottomSheet() {
        EquipmentBottomSheetFragment().show(parentFragmentManager, "bs_equipment")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeAsUpEnabled(true)
    }

    private fun saveToFile() {
        EquipmentListFile.saveToFile(recyclerView.context, equipments.toTypedArray(), sharedPreferences)
    }

    private fun showHelpBottomSheet() {
        GenericBottomSheetFragment.create(getString(R.string.bs_help_equipment), R.string.bs_help_equipment_title, android.R.string.ok).show(childFragmentManager, "bs_equipment")
        sharedPreferences?.edit()?.putBoolean(PREF_KEY_EQUIPMENT_HELP_SHOWN, true)?.apply()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_help, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_help -> {
                showHelpBottomSheet()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}