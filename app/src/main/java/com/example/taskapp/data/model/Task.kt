package com.example.taskapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    var id: String = "",
    var description : String = "",
    var status: Status = Status.TODO

) : Parcelable


