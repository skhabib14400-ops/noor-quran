package com.example.prayer

data class CityLocation(
    val nameEn: String,
    val nameBn: String,
    val nameAr: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val country: String
) {
    companion object {
        val MONOHARGONJ = CityLocation(
            nameEn = "Monohargonj",
            nameBn = "মনোহরগঞ্জ",
            nameAr = "منوهرغنج",
            latitude = 23.1895,
            longitude = 91.1347,
            timeZoneId = "Asia/Dhaka",
            country = "Bangladesh"
        )

        val DEFAULT_DHAKA = CityLocation(
            nameEn = "Dhaka",
            nameBn = "ঢাকা",
            nameAr = "دكا",
            latitude = 23.8103,
            longitude = 90.4125,
            timeZoneId = "Asia/Dhaka",
            country = "Bangladesh"
        )

        val PRESET_CITIES = listOf(
            DEFAULT_DHAKA,
            MONOHARGONJ,
            CityLocation("Cumilla", "কুমিল্লা", "كوميلا", 23.4682, 91.1788, "Asia/Dhaka", "Bangladesh"),
            CityLocation("Chittagong", "চট্টগ্রাম", "شيتاغونغ", 22.3569, 91.7832, "Asia/Dhaka", "Bangladesh"),
            CityLocation("Sylhet", "সিলেট", "سيلهيت", 24.8949, 91.8687, "Asia/Dhaka", "Bangladesh"),
            CityLocation("Khulna", "খুলনা", "خولنا", 22.8456, 89.5403, "Asia/Dhaka", "Bangladesh"),
            CityLocation("Rajshahi", "রাজশাহী", "راجشاهي", 24.3745, 88.6042, "Asia/Dhaka", "Bangladesh"),
            CityLocation("Barisal", "বরিশাল", "باريسال", 22.7010, 90.3535, "Asia/Dhaka", "Bangladesh"),
            CityLocation("Rangpur", "রংপুর", "رانجبور", 25.7439, 89.2752, "Asia/Dhaka", "Bangladesh"),
            CityLocation("Mymensingh", "ময়মনসিংহ", "ميمينسينغ", 24.7471, 90.4203, "Asia/Dhaka", "Bangladesh"),
            CityLocation("Makkah", "মক্কা", "مكة المكرمة", 21.4225, 39.8262, "Asia/Riyadh", "Saudi Arabia"),
            CityLocation("Madinah", "মদিনা", "المدينة المنورة", 24.5247, 39.5692, "Asia/Riyadh", "Saudi Arabia"),
            CityLocation("Cairo", "কায়রো", "القاهرة", 30.0444, 31.2357, "Africa/Cairo", "Egypt"),
            CityLocation("Dubai", "দুবাই", "دبي", 25.2048, 55.2708, "Asia/Dubai", "UAE"),
            CityLocation("Kuala Lumpur", "কুয়ালালামপুর", "كوالالمبور", 3.1390, 101.6869, "Asia/Kuala_Lumpur", "Malaysia"),
            CityLocation("Jakarta", "জাকার্তা", "جاكرتا", -6.2088, 106.8456, "Asia/Jakarta", "Indonesia"),
            CityLocation("London", "লন্ডন", "لندن", 51.5074, -0.1278, "Europe/London", "United Kingdom"),
            CityLocation("New York", "নিউইয়র্ক", "نيويورك", 40.7128, -74.0060, "America/New_York", "United States"),
            CityLocation("Toronto", "টরন্টো", "تورونتو", 43.6532, -79.3832, "America/Toronto", "Canada"),
            CityLocation("Sydney", "সিডনি", "سيدني", -33.8688, 151.2093, "Australia/Sydney", "Australia"),
            CityLocation("Tokyo", "টোকিও", "طوكيو", 35.6762, 139.6503, "Asia/Tokyo", "Japan")
        )
    }
}
