package com.example.todoapp.view.fragments.update

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentUpdateBinding
import com.example.todoapp.model.ToDoData
import com.example.todoapp.viewmodel.SharedViewModel
import com.example.todoapp.viewmodel.ToDoViewModel

class UpdateFragment : Fragment() {
    private val args by navArgs<UpdateFragmentArgs>()
    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuhost: MenuHost = requireActivity()
        menuhost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.update_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_Save -> updateItem()
                    R.id.menu_delete -> confirmItemRemoval()
                    android.R.id.home -> requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                return true
            }

        },viewLifecycleOwner, Lifecycle.State.RESUMED)

        with(binding) {
            currentPrioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener
            currentNoteTitle.setText(args.currentItem.title)
            currentNoteDescription.setText(args.currentItem.description)
            currentPrioritiesSpinner.setSelection(mSharedViewModel.parsePriorityToInt(args.currentItem.priority))
        }

    }

    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setMessage("Are you sure you want to remove ${args.currentItem.title}?")
            setTitle("Delete ${args.currentItem.title}?")
            setNegativeButton("Cancel") { _, _ -> }
            setPositiveButton("Yes") { _, _ ->
                // Remove item
                mToDoViewModel.deleteItem(args.currentItem)
                Toast.makeText(
                    requireContext(),
                    "Removed : ${args.currentItem.title} !",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_updateFragment_to_listFragment)
            }
            create().show()
        }


    }

    private fun updateItem() {
        val title = binding.currentNoteTitle.text.toString()
        val description = binding.currentNoteDescription.text.toString()
        val getPriority = binding.currentPrioritiesSpinner.selectedItem.toString()

        val validation = mSharedViewModel.verifyDataFromUser(title, description)
        if (validation) {
            // Update current item
            val updatedItem = ToDoData(
                args.currentItem.id,
                title,
                mSharedViewModel.parsePriority(getPriority),
                description
            )
            mToDoViewModel.updateData(updatedItem)
            Toast.makeText(requireContext(), "Succesfully Updated!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please Fill Out All Fields!", Toast.LENGTH_SHORT)
                .show()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}