package com.example.domain

import com.example.data.MockData
import com.example.data.MunicipalAreaAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MunicipalRepository {
    fun getMunicipalAnalytics(): Flow<List<MunicipalAreaAnalytics>> = flow {
        emit(MockData.municipalData)
    }
}
