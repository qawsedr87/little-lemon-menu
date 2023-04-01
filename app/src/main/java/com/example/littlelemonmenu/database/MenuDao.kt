package com.example.littlelemonmenu.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.littlelemonmenu.data.MenuItem

@Dao
interface MenuDao {
    @Insert
    fun insert(item: MenuItem)

    @Insert
    fun insertAll(vararg items: MenuItem)

    @Query("SELECT * FROM menu_table")
    fun getAll(): LiveData<List<MenuItem>>

    @Update
    fun update(item: MenuItem)

    @Delete
    fun delete(item: MenuItem)

    @Query("SELECT (SELECT COUNT(*) FROM menu_table) == 0")
    fun isEmpty(): Boolean

}