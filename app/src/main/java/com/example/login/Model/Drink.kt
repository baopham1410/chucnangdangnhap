package com.example.login.Model

import android.os.Parcel
import android.os.Parcelable

data class Drink(
    val id: String = "", // Thêm thuộc tính ID kiểu String, có giá trị mặc định là ""
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val imageResId: String = "",
    var category: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // Đọc ID từ Parcel
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id) // Ghi ID vào Parcel
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(price)
        parcel.writeString(imageResId)
        parcel.writeString(category)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Drink> {
        override fun createFromParcel(parcel: Parcel): Drink {
            return Drink(parcel)
        }

        override fun newArray(size: Int): Array<Drink?> {
            return arrayOfNulls(size)
        }
    }
}