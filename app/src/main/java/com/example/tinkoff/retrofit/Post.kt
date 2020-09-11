package com.example.tinkoff.retrofit

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
 data class Post
  (
  @SerializedName("description") val desc: String?=null,
  @SerializedName("gifURL") val gifUrl: String?=null
) : Parcelable









