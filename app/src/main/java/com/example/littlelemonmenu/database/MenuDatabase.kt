package com.example.littlelemonmenu.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.littlelemonmenu.data.MenuItem

@Database(entities = [MenuItem::class], version = 1)
abstract class MenuDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
}