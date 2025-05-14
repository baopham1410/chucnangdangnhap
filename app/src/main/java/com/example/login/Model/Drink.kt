package com.example.login.Model

import android.os.Parcel
import android.os.Parcelable

data class Drink(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val imageResId: String = "",
    var category: String = "",
    var documentId: String? = null,
    var quantity: Int = 0 // Thêm trường quantity kiểu Int, có giá trị mặc định là 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(), // Đọc documentId từ Parcel
        parcel.readInt() // Đọc quantity từ Parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(price)
        parcel.writeString(imageResId)
        parcel.writeString(category)
        parcel.writeString(documentId)
        parcel.writeInt(quantity) // Ghi quantity vào Parcel
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