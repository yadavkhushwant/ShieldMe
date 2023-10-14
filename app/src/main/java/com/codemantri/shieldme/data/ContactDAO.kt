package com.codemantri.shieldme.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDAO {

    @Insert
    fun insertContact(contact: ContactEntity)

    @Update
    fun updateContact(contact: ContactEntity)

    @Delete
    fun deleteContact(contactEntity: ContactEntity)

    @Query("SELECT * FROM contacts ORDER BY name COLLATE NOCASE ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT number FROM contacts")
    fun getContactNumbers(): List<String>
}