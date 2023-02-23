package com.example.todoapp.view.fragments.add

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentAddBinding
import com.example.todoapp.model.Priority
import com.example.todoapp.model.ToDoData
import com.example.todoapp.viewmodel.SharedViewModel
import com.example.todoapp.viewmodel.ToDoViewModel

class AddFragment : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Set Menu
        setHasOptionsMenu(true)
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.prioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener


    }

    // Add
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_add) {
            insertDataToDatabase()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertDataToDatabase() {
        val mTitle = binding.editNoteTitle.text.toString()
        val mDescription = binding.editNoteDescription.text.toString()
        val mPriority = binding.prioritiesSpinner.selectedItem.toString()

        val validation = mSharedViewModel.verifyDataFromUser(mTitle, mDescription)
        if (validation) {
            // Insert data to Database
            val newData = ToDoData(
                0,
                mTitle,
                mSharedViewModel.parsePriority(mPriority),
                mDescription
            )
            mToDoViewModel.insertData(newData)
            Toast.makeText(requireContext(), "Succesfuly Added!", Toast.LENGTH_SHORT).show()
            // Navigate Back
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please Fill Out All Fields.", Toast.LENGTH_SHORT).show()
        }
    }



    // Set Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}