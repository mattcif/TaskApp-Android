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
import com.example.taskapp.util.showBottomSheet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TaskViewModel : ViewModel() {

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>> = _taskList

    private val _taskInsert = MutableLiveData<Task>()
    val taskInsert: LiveData<Task> = _taskInsert

    private val _taskUpdate = MutableLiveData<Task>()
    val taskUpdate: LiveData<Task> = _taskUpdate

    fun setUpdateTask(task: Task) {
        _taskUpdate.postValue(task)
    }

    fun getTasks(status: Status) {
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
                    _taskList.postValue(taskList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("INFOTESTE", "onCancelled:")
                }

            })


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
}