package com.example.havadurumu.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NominatimApi {
    @Headers("User-Agent: CloudHeart/1.0 (Bilisim Kampi Android ogrenci projesi)")
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("countrycodes") countryCodes: String = "tr",
        @Query("limit") limit: Int = 1,
        @Query("accept-language") language: String = "tr",
        @Query("addressdetails") addressDetails: Int = 1
    ): List<NominatimResult>
}

data class NominatimResult(
    val name: String?,
    val lat: String,
    val lon: String,
    val addresstype: String?,
    val address: NominatimAddress?
)

data class NominatimAddress(
    val province: String?,
    val state: String?,
    val town: String?,
    @SerializedName("city_district") val cityDistrict: String?
)
