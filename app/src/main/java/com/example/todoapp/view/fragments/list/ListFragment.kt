package com.example.todoapp.view.fragments.list

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.adapter.ListAdapter
import com.example.todoapp.databinding.FragmentListBinding
import com.example.todoapp.model.ToDoData
import com.example.todoapp.viewmodel.SharedViewModel
import com.example.todoapp.viewmodel.ToDoViewModel
import com.google.android.material.snackbar.Snackbar

class ListFragment : Fragment() {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()
    private val adapter: ListAdapter by lazy { ListAdapter() }
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        mToDoViewModel.getAllData.observe(viewLifecycleOwner) {
            mSharedViewModel.checkIfDatabaseEmpty(it)
            adapter.setData(it)
        }
        mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner) {
            showEmptyDatabaseView(it)
        }

        // Set Menu
        setHasOptionsMenu(true)

        binding.addNoteButton.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToAddFragment()
            Navigation.findNavController(it).navigate(action)
        }
    }

    fun setupRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        swipeToDelete(recyclerView)
    }

    private fun restoreDeletedData(view: View, deletedItem: ToDoData, position: Int) {
        val snackBar = Snackbar.make(
            view,
            "Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
            adapter.notifyItemChanged(position)
        }
        snackBar.show()
    }

    fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallBack = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                // Delete item
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                // Restore deleted item
                restoreDeletedData(viewHolder.itemView,deletedItem,viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showEmptyDatabaseView(emptyDatabase: Boolean) {
        if (emptyDatabase) {
            with(binding) {
                noDataImageView.visibility = View.VISIBLE
                noDataTextView.visibility = View.VISIBLE
            }
        } else {
            with(binding) {
                noDataImageView.visibility = View.GONE
                noDataTextView.visibility = View.GONE
            }
        }
    }

    // Set Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_delete_all) {
            confirmRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setMessage("Are you sure you want to remove everything?")
            setTitle("Delete Everything?")
            setNegativeButton("Cancel") { _, _ -> }
            setPositiveButton("Yes") { _, _ ->
                // Remove item
                mToDoViewModel.deleteAll()
                Toast.makeText(
                    requireContext(),
                    "Removed Everything!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            create().show()
        }
    }

}