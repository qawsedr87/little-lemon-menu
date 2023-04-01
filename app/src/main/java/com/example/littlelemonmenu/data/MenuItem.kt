package com.example.littlelemonmenu.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "menu_table")
data class MenuItem(
    @PrimaryKey
    var id: String,

    var name: String,
    var price: Double
)


@Serializable
data class MenuItemNetwork(
    // from api
    var id: Int,
    var title: String,
    var price: Double
) {
    fun toMenuItem() = MenuItem(
        id = id.toString(),
        name = title,
        price = price
    )
}

