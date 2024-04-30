package com.example.taskapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taskapp.data.model.Status
import com.example.taskapp.data.model.Task
import com.example.taskapp.util.FirebaseHelper
import com.example.taskapp.util.StateView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TaskViewModel : ViewModel() {

    private val _taskList = MutableLiveData<StateView<List<Task>>>()
    val taskList: LiveData<StateView<List<Task>>> = _taskList

    private val _taskInsert = MutableLiveData<StateView<Task>>()
    val taskInsert: LiveData<StateView<Task>> = _taskInsert

    private val _taskUpdate = MutableLiveData<StateView<Task>>()
    val taskUpdate: LiveData<StateView<Task>> = _taskUpdate

    private val _taskDelete = MutableLiveData<StateView<Task>>()
    val taskDelete: LiveData<StateView<Task>> = _taskDelete



    fun getTasks() {

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
                            taskList.add(task)
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
         try {
             _taskInsert.postValue(StateView.OnLoading())


             FirebaseHelper.getDatabase()
                 .child("tasks")
                 .child(FirebaseHelper.getIdUser())
                 .child(task.id)
                 .setValue(task).addOnCompleteListener { result ->
                     if (result.isSuccessful) {
                         _taskInsert.postValue(StateView.OnSuccess(task))
                     }
                 }
         } catch (ex: Exception) {
             _taskInsert.postValue(StateView.OnError(ex.message.toString()))

         }
    }

    fun updateTask(task: Task) {

        try {

            _taskUpdate.postValue(StateView.OnLoading())

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
                        _taskUpdate.postValue(StateView.OnSuccess(task))
                    }
                }
        } catch (ex: Exception) {
            _taskUpdate.postValue(StateView.OnError(ex.message.toString()))
        }
    }

    fun deleteTask(task: Task) {

        try {
            _taskDelete.postValue(StateView.OnLoading())

            FirebaseHelper.getDatabase()
                .child("tasks")
                .child(FirebaseHelper.getIdUser())
                .child(task.id)
                .removeValue().addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        _taskDelete.postValue(StateView.OnSuccess(task))

                    }
                }
        } catch (ex: Exception) {
            _taskDelete.postValue(StateView.OnError(ex.message.toString()))
        }
    }


}