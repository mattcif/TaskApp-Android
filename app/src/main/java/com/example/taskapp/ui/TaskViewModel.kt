package com.example.taskapp.ui

import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.taskapp.R
import com.example.taskapp.data.model.Status
import com.example.taskapp.data.model.Task
import com.example.taskapp.util.FirebaseHelper
import com.example.taskapp.util.StateView
import com.example.taskapp.util.showBottomSheet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TaskViewModel : ViewModel() {

    private val _taskList = MutableLiveData<StateView<List<Task>>>()
    val taskList: LiveData<StateView<List<Task>>> = _taskList

    private val _taskInsert = MutableLiveData<Task>()
    val taskInsert: LiveData<Task> = _taskInsert

    private val _taskUpdate = MutableLiveData<Task>()
    val taskUpdate: LiveData<Task> = _taskUpdate

    private val _taskDelete = MutableLiveData<Task>()
    val taskDelete: LiveData<Task> = _taskDelete



    fun getTasks(status: Status) {

        try {

            _taskList.postValue(StateView.OnLoading())

            FirebaseHelper.getDatabase()
                .child("tasks")
                .child(FirebaseHelper.getIdUser())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val taskList = mutableListOf<Task>()
                        for (ds in snapshot.children) {
                            val task = ds.getValue(Task::class.java) as Task
                            if (task.status == status) {
                                taskList.add(task)
                            }
                        }
                        taskList.reverse()
                        _taskList.postValue(StateView.OnSuccess(taskList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i("INFOTESTE", "onCancelled:")
                    }

                })
        } catch (ex: Exception){
            _taskList.postValue(StateView.OnError(ex.message.toString()))

        }




    }

     fun insertTask(task: Task) {
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .setValue(task).addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    _taskInsert.postValue(task)
                }
            }

    }

    fun updateTask(task: Task) {
        val map = mapOf(
            "description" to task.description,
            "status" to task.status
        )

        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .updateChildren(map).addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    _taskUpdate.postValue(task)
                }
            }
    }

    fun deleteTask(task: Task) {
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .removeValue().addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    _taskDelete.postValue(task)

                }
            }

    }


}