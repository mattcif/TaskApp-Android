package com.example.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskapp.R
import com.example.taskapp.data.model.Status
import com.example.taskapp.data.model.Task
import com.example.taskapp.databinding.FragmentTodoBinding
import com.example.taskapp.ui.adapter.TaskAdapter
import com.example.taskapp.util.FirebaseHelper
import com.example.taskapp.util.showBottomSheet


class TodoFragment : Fragment() {

    private var _binding: FragmentTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter


    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        initListeners()

        initRecyclerView()

        observeViewModel()

        viewModel.getTasks(Status.TODO)

    }

    private fun initListeners() {
        binding.fabAdd.setOnClickListener {
            val action = HomeFragmentDirections
                .actionHomeFragmentToFormTaskFragment(null)
            findNavController().navigate(action)
        }

    }

    private fun observeViewModel() {

        viewModel.taskList.observe(viewLifecycleOwner) {taskList ->
            binding.progressBar.isVisible = false
            listEmpty(taskList)

            taskAdapter.submitList(taskList)
        }

        viewModel.taskInsert.observe(viewLifecycleOwner) { task ->
            if (task.status == Status.TODO) {
                // armazena a lista atual do adapter
                val oldList = taskAdapter.currentList

                // gera uma nova lista a partir da lista antiga já com a tarefa atualizada
                val newList = oldList.toMutableList().apply {
                    add(0, task)
                }


                // Envia a lista atualizada para o adapter
                taskAdapter.submitList(newList)


                setPositionRecyclerView()
            }
        }

        viewModel.taskUpdate.observe(viewLifecycleOwner) { updateTask ->
            // armazena a lista atual do adapter
            val oldList = taskAdapter.currentList

            // gera uma nova lista a partir da lista antiga já com a tarefa atualizada
            val newList = oldList.toMutableList().apply {
                if (updateTask.status == Status.TODO) {
                    find {it.id == updateTask.id}?.description = updateTask.description
                } else {
                    remove(updateTask)
                }
            }

            // armazena a posição da tarefa a ser atualizada na lista
            val position = newList.indexOfFirst { it.id == updateTask.id }

            // Envia a lista atualizada para o adapter
            taskAdapter.submitList(newList)

            // Atualiza a tarefa pela posição do adapter
            taskAdapter.notifyItemChanged(position)
        }
    }

    private fun initRecyclerView() {
        taskAdapter = TaskAdapter(requireContext()) { task, option ->
            optionSelected(task, option)

        }

        with(binding.rvTasks) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = taskAdapter
        }


    }


    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_REMOVE -> {
                showBottomSheet(
                    titleDialog = R.string.text_title_dialog_delete,
                    message = getString(R.string.text_message_dialog_delete),
                    titleButtom = R.string.text_button_dialog_confirm,
                    onClick = {
                        deleteTask(task)
                    }

                )
            }

            TaskAdapter.SELECT_EDIT -> {
                val action = HomeFragmentDirections
                    .actionHomeFragmentToFormTaskFragment(task)
                findNavController().navigate(action)

            }

            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(requireContext(), "Detalhes ${task.description}", Toast.LENGTH_SHORT)
                    .show()

            }

            TaskAdapter.SELECT_NEXT -> {
                task.status = Status.DOING
                viewModel.updateTask(task)

            }
        }
    }


    private fun setPositionRecyclerView() {
        taskAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {

            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvTasks.scrollToPosition(0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            }
        })

    }


    private fun deleteTask(task: Task) {
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .removeValue().addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        R.string.text_delete_success_task,
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT)
                        .show()
                }
            }

    }



    private fun listEmpty(taskList: List<Task>) {
        binding.texInfo.text = if (taskList.isEmpty()) {
            getString(R.string.text_list_task_empty)

        } else {
            ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}