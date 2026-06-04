package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class UrbanParkingApiResponse(
    val status: String,
    val zones: List<ApiZoneData>
)

data class ApiZoneData(
    val zoneId: String,
    val probability: Double,
    val freshness: Int
)

interface UrbanParkingApi {
    @GET("parking/v1/availability")
    suspend fun getAvailability(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): UrbanParkingApiResponse
}

object UrbanApiServiceFactory {
    private const val BASE_URL = "https://api.mockurbanparking.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        
    val api: UrbanParkingApi by lazy {
        retrofit.create(UrbanParkingApi::class.java)
    }
}
