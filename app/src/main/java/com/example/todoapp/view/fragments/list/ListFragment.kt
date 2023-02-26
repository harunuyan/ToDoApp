package com.example.todoapp.view.fragments.list

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentListBinding
import com.example.todoapp.model.ToDoData
import com.example.todoapp.utils.hideKeyboard
import com.example.todoapp.utils.observeOnce
import com.example.todoapp.view.fragments.list.adapter.ListAdapter
import com.example.todoapp.viewmodel.SharedViewModel
import com.example.todoapp.viewmodel.ToDoViewModel
import com.google.android.material.snackbar.Snackbar

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

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
            binding.recyclerView.scheduleLayoutAnimation()
        }
        mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner) {
            showEmptyDatabaseView(it)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.list_fragment_menu, menu)

                // Search
                val search = menu.findItem(R.id.menu_search)
                val searchView = search.actionView as? SearchView
                searchView?.isSubmitButtonEnabled = true
                searchView?.setOnQueryTextListener(this@ListFragment)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_delete_all -> confirmRemoval()
                    R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(
                        viewLifecycleOwner
                    ) {
                        adapter.setData(it)
                    }
                    R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(
                        viewLifecycleOwner
                    ) {
                        adapter.setData(it)
                    }
                    android.R.id.home -> requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                return true
            }

        },viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Hide Soft Keyboard
        hideKeyboard(requireActivity())

        binding.addNoteButton.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToAddFragment()
            Navigation.findNavController(it).navigate(action)
        }
    }

    fun setupRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        // Swipe to delete
        swipeToDelete(recyclerView)
    }

    private fun restoreDeletedData(view: View, deletedItem: ToDoData) {
        val snackBar = Snackbar.make(
            view,
            "Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
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
                restoreDeletedData(viewHolder.itemView, deletedItem)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughtDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughtDatabase(query)
        }
        return true
    }

    private fun searchThroughtDatabase(query: String) {
        val searchQuery = "%$query%"
        mToDoViewModel.searchQuery(searchQuery).observeOnce(viewLifecycleOwner) {
            it?.let {
                adapter.setData(it)
            }
        }
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