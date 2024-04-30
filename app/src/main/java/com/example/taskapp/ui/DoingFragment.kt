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
import com.example.taskapp.databinding.FragmentDoingBinding
import com.example.taskapp.ui.adapter.TaskAdapter
import com.example.taskapp.util.StateView
import com.example.taskapp.util.showBottomSheet


class DoingFragment : Fragment() {
    private var _binding: FragmentDoingBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private val viewModel: TaskViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        initRecyclerView()

        observeViewModel()

        viewModel.getTasks()
    }


    private fun observeViewModel() {
        viewModel.taskList.observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.OnLoading -> {
                    binding.progressBar.isVisible = true
                }
                is StateView.OnSuccess -> {

                    val taskList = stateView.data?.filter { it.status == Status.DOING }

                    binding.progressBar.isVisible = false
                    listEmpty(taskList ?: emptyList())

                    taskAdapter.submitList(taskList)
                }
                is StateView.OnError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false
                }
            }



        }

        viewModel.taskInsert.observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.OnLoading -> {
                    binding.progressBar.isVisible = true
                }
                is StateView.OnSuccess -> {

                    binding.progressBar.isVisible = false

                    if(stateView.data?.status == Status.DOING) {
                        // Armazena a lista atual do adapter
                        val oldList = taskAdapter.currentList

                        // Gera uma nova lista a partir da lista antiga já com a tarefa atualizada
                        val newList = oldList.toMutableList().apply {
                            add(0, stateView.data)
                        }

                        // Envia a lista atualizada para o adapter
                        taskAdapter.submitList(newList)

                        setPositionRecyclerView()
                    }
                }
                is StateView.OnError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false
                }
            }
        }

        viewModel.taskUpdate.observe(viewLifecycleOwner) { stateView ->

            when(stateView) {
                is StateView.OnLoading -> {
                    binding.progressBar.isVisible = true
                }
                is StateView.OnSuccess -> {
                    binding.progressBar.isVisible = false

                    // armazena a lista atual do adapter
                    val oldList = taskAdapter.currentList

                    // gera uma nova lista a partir da lista antiga já com a tarefa atualizada
                    val newList = oldList.toMutableList().apply {
                        if(!oldList.contains(stateView.data) && stateView.data?.status == Status.DOING){
                            add(0, stateView.data)
                            setPositionRecyclerView()
                        }
                        if (stateView.data?.status == Status.DOING) {
                            find { it.id == stateView.data.id }?.description = stateView.data.description
                        } else {
                            remove(stateView.data)
                        }
                    }

                    // armazena a posição da tarefa a ser atualizada na lista
                    val position = newList.indexOfFirst { it.id == stateView.data?.id }

                    // Envia a lista atualizada para o adapter
                    taskAdapter.submitList(newList)

                    // Atualiza a tarefa pela posição do adapter
                    taskAdapter.notifyItemChanged(position)

                }
                is StateView.OnError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false
                }
            }


        }

        viewModel.taskDelete.observe(viewLifecycleOwner) { stateView ->

            when(stateView) {
                is StateView.OnLoading -> {
                    binding.progressBar.isVisible = true
                }
                is StateView.OnSuccess -> {
                    binding.progressBar.isVisible = false

                    Toast.makeText(requireContext(), R.string.text_delete_success_task, Toast.LENGTH_SHORT)
                        .show()

                    val oldList = taskAdapter.currentList
                    val newList = oldList.toMutableList().apply {
                        remove(stateView.data)
                    }

                    taskAdapter.submitList(newList)

                }
                is StateView.OnError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false
                }
            }



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
        when(option) {
            TaskAdapter.SELECT_BACK -> {
                task.status = Status.TODO
                viewModel.updateTask(task)
            }
            TaskAdapter.SELECT_REMOVE -> {
                showBottomSheet(
                    titleDialog = R.string.text_title_dialog_delete,
                    message = getString(R.string.text_message_dialog_delete),
                    titleButtom = R.string.text_button_dialog_confirm,
                    onClick = {
                        viewModel.deleteTask(task)
                    }

                )
            }
            TaskAdapter.SELECT_EDIT -> {
                val action = HomeFragmentDirections
                    .actionHomeFragmentToFormTaskFragment(task)
                findNavController().navigate(action)

            }
            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(requireContext(), "Detalhes ${task.description}", Toast.LENGTH_SHORT).show()

            }
            TaskAdapter.SELECT_NEXT -> {
                task.status = Status.DONE
                viewModel.updateTask(task)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}