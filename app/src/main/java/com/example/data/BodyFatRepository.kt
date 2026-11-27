package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BodyFatRepository(
    private val scanRecordDao: ScanRecordDao,
    private val userProfileDao: UserProfileDao
) {
    val allScanRecords: Flow<List<ScanRecord>> = scanRecordDao.getAllScanRecords()
    val latestScanRecord: Flow<ScanRecord?> = scanRecordDao.getLatestScanRecord()
    val userProfileFlow: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()

    suspend fun insertScanRecord(record: ScanRecord): Long = withContext(Dispatchers.IO) {
        scanRecordDao.insertScanRecord(record)
    }

    suspend fun deleteScanRecord(record: ScanRecord) = withContext(Dispatchers.IO) {
        scanRecordDao.deleteScanRecord(record)
    }

    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        scanRecordDao.clearAllRecords()
    }

    suspend fun getUserProfileDirect(): UserProfile? = withContext(Dispatchers.IO) {
        userProfileDao.getUserProfile()
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.insertUserProfile(profile)
    }
}
