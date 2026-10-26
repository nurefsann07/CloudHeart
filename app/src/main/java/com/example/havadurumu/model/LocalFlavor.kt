package com.example.havadurumu.model

object LocalFlavor {
    data class CityTips(
        val outdoor: List<String>,
        val indoor: List<String>,
        val taste: List<String>
    )

    enum class Region(val note: String?) {
        MARMARA(null),
        EGE(null),
        AKDENIZ("Güneş güçlü olabilir, yanına su al"),
        KARADENIZ("Karadeniz'de hava çabuk değişir, yağmurluğun çantanda dursun"),
        IC_ANADOLU("İç Anadolu'da gece ile gündüz arası fark büyük, akşam için bir kat fazla al"),
        DOGU_ANADOLU("Doğu'da akşamları çok serin olur, kalın bir şey al"),
        GUNEYDOGU("Gündüz sıcak geçebilir, açık renk ve ince giysiler seç")
    }

    private val cities: Map<String, CityTips> = mapOf(
        "istanbul" to CityTips(
            outdoor = listOf("Vapurla Boğaz'da kısa bir tur yap", "Moda sahilinde yürüyüş", "Bebek'ten Arnavutköy'e sahil yürüyüşü", "Emirgan Korusu'nda dolaş"),
            indoor = listOf("İstanbul Modern'i gez", "Kapalıçarşı'da dolaş", "Pera Müzesi'ne uğra", "Kadıköy'de bir kafede kitap oku"),
            taste = listOf("Karaköy'de simit ve çay", "Eminönü'nde balık ekmek", "Kadıköy çarşısında kahve")
        ),
        "ankara" to CityTips(
            outdoor = listOf("Kuğulu Park'ta yürüyüş", "Anıtkabir'i ziyaret et", "Gençlik Parkı'nda dolaş", "Eymir Gölü çevresinde yürü"),
            indoor = listOf("Anadolu Medeniyetleri Müzesi'ni gez", "Kızılay'da bir kitapçıda vakit geçir", "Erimtan Arkeoloji Müzesi'ne uğra"),
            taste = listOf("Ankara tava dene", "Hamamönü'nde kahve iç")
        ),
        "izmir" to CityTips(
            outdoor = listOf("Kordon'da gün batımını izle", "Alsancak'ta bisiklet sür", "Kemeraltı'nda dolaş", "Karşıyaka'ya vapurla geç"),
            indoor = listOf("Kemeraltı'nda tarihi bir hana uğra", "Arkeoloji Müzesi'ni gez", "Alsancak'ta bir kafede ders çalış"),
            taste = listOf("Boyoz ve gevrek ile kahvaltı yap", "Kumru dene")
        ),
        "antalya" to CityTips(
            outdoor = listOf("Konyaaltı sahilinde yürüyüş", "Kaleiçi'nin sokaklarında dolaş", "Düden Şelalesi'ne git", "Karaalioğlu Parkı'nda gün batımı"),
            indoor = listOf("Antalya Müzesi'ni gez", "Kaleiçi'nde bir kafede otur"),
            taste = listOf("Piyaz ve şiş köfte dene", "Taze portakal suyu iç")
        ),
        "bursa" to CityTips(
            outdoor = listOf("Uludağ'a teleferikle çık", "Cumalıkızık köyünü gez", "Koza Han'ın avlusunda otur"),
            indoor = listOf("Ulu Cami'yi ziyaret et", "Kapalı Çarşı'da dolaş", "Tarihi bir hamamı gör"),
            taste = listOf("İskender kebap ye", "Kestane şekeri al")
        ),
        "eskisehir" to CityTips(
            outdoor = listOf("Porsuk Çayı'nda gondol turu", "Odunpazarı evleri arasında dolaş", "Sazova Parkı'na git"),
            indoor = listOf("Odunpazarı Modern Müze'yi gez", "Cam Sanatları Müzesi'ne uğra"),
            taste = listOf("Çibörek dene", "Met helvası tat")
        ),
        "trabzon" to CityTips(
            outdoor = listOf("Uzungöl'e git", "Boztepe'den şehri izle", "Sümela Manastırı'nı gez"),
            indoor = listOf("Atatürk Köşkü'nü ziyaret et", "Ayasofya Müzesi'ne uğra"),
            taste = listOf("Akçaabat köftesi ye", "Kuymak dene")
        ),
        "rize" to CityTips(
            outdoor = listOf("Ayder Yaylası'na çık", "Zil Kale'yi gör", "Çay bahçelerinde yürü"),
            indoor = listOf("Bir çay fabrikasını gez", "Sıcak bir mekânda demli çay iç"),
            taste = listOf("Muhlama dene", "Rize simidi ile çay iç")
        ),
        "gaziantep" to CityTips(
            outdoor = listOf("Gaziantep Kalesi'ne çık", "Bakırcılar Çarşısı'nda dolaş"),
            indoor = listOf("Zeugma Mozaik Müzesi'ni gez", "Emine Göğüş Mutfak Müzesi'ne uğra"),
            taste = listOf("Baklava ye", "Beyran çorbası iç", "Menengiç kahvesi dene")
        ),
        "konya" to CityTips(
            outdoor = listOf("Alaeddin Tepesi'nde yürü", "Kelebekler Vadisi'ni gez"),
            indoor = listOf("Mevlana Müzesi'ni ziyaret et", "Karatay Medresesi'ni gez"),
            taste = listOf("Etli ekmek ye", "Fırın kebabı dene")
        ),
        "nevsehir" to CityTips(
            outdoor = listOf("Kapadokya'da balonları izle", "Güvercinlik Vadisi'nde yürü", "Uçhisar Kalesi'ne çık"),
            indoor = listOf("Derinkuyu Yeraltı Şehri'ni gez", "Avanos'ta çömlek atölyesine katıl"),
            taste = listOf("Testi kebabı dene")
        ),
        "mugla" to CityTips(
            outdoor = listOf("Bodrum'da sahil yürüyüşü", "Fethiye'de Ölüdeniz'e git", "Akyaka'da Azmak'ta tekne turu"),
            indoor = listOf("Bodrum Sualtı Arkeoloji Müzesi'ni gez", "Muğla'nın eski evleri arasında bir kafeye otur"),
            taste = listOf("Çökertme kebabı dene")
        ),
        "canakkale" to CityTips(
            outdoor = listOf("Kordon'da yürü ve Truva atını gör", "Gelibolu Yarımadası'nı gez", "Bozcaada'ya feribotla geç"),
            indoor = listOf("Çanakkale Savaşları Müzesi'ni gez", "Aynalı Çarşı'da dolaş"),
            taste = listOf("Peynir helvası dene")
        ),
        "edirne" to CityTips(
            outdoor = listOf("Meriç Köprüsü'nde gün batımını izle", "Kaleiçi'nde dolaş"),
            indoor = listOf("Selimiye Camii'ni gez", "Sağlık Müzesi'ne uğra"),
            taste = listOf("Tava ciğer ye", "Badem ezmesi al")
        ),
        "mardin" to CityTips(
            outdoor = listOf("Eski Mardin sokaklarında dolaş", "Dara Antik Kenti'ni gez"),
            indoor = listOf("Zinciriye Medresesi'ni gez", "Mardin Müzesi'ne uğra"),
            taste = listOf("Kaburga dolması dene", "Mırra kahvesi iç")
        ),
        "hatay" to CityTips(
            outdoor = listOf("Harbiye Şelalesi'ne git", "Samandağ sahilinde yürü"),
            indoor = listOf("Hatay Arkeoloji Müzesi'ni gez"),
            taste = listOf("Künefe ye", "Humus ve muhammara dene")
        ),
        "adana" to CityTips(
            outdoor = listOf("Seyhan kıyısında Taşköprü'de yürü", "Merkez Park'ta dolaş"),
            indoor = listOf("Adana Müze Kompleksi'ni gez"),
            taste = listOf("Adana kebap ye", "Bici bici ile serinle")
        ),
        "mersin" to CityTips(
            outdoor = listOf("Sahil boyunca bisiklet sür", "Kızkalesi'ni gör"),
            indoor = listOf("Cennet ve Cehennem Obrukları'nın ziyaretçi merkezine uğra", "Mersin Kent Müzesi'ni gez"),
            taste = listOf("Tantuni ye", "Cezerye tat")
        ),
        "kayseri" to CityTips(
            outdoor = listOf("Erciyes'te doğa yürüyüşü", "Talas'ın tarihi sokaklarında dolaş"),
            indoor = listOf("Kayseri Kalesi'ndeki çarşıyı gez", "Gevher Nesibe Müzesi'ne uğra"),
            taste = listOf("Mantı ye", "Pastırma tat")
        ),
        "samsun" to CityTips(
            outdoor = listOf("Atakum sahilinde yürü", "Amisos Tepesi'ne çık"),
            indoor = listOf("Bandırma Vapuru Müzesi'ni gez"),
            taste = listOf("Samsun pidesi ye")
        ),
        "erzurum" to CityTips(
            outdoor = listOf("Palandöken'e çık", "Çifte Minareli Medrese'nin önünde fotoğraf çek"),
            indoor = listOf("Taş Mağazalar'da dolaş", "Yakutiye Medresesi'ni gez"),
            taste = listOf("Cağ kebabı ye", "Kadayıf dolması dene")
        ),
        "van" to CityTips(
            outdoor = listOf("Van Kalesi'nden gölü izle", "Akdamar Adası'na tekneyle geç"),
            indoor = listOf("Van Müzesi'ni gez"),
            taste = listOf("Van kahvaltısı yap")
        ),
        "sanliurfa" to CityTips(
            outdoor = listOf("Balıklıgöl'de dolaş", "Göbeklitepe'yi gez"),
            indoor = listOf("Şanlıurfa Arkeoloji Müzesi'ni gez", "Gümrük Hanı'nda mırra iç"),
            taste = listOf("Çiğ köfte ye", "Urfa kebabı dene")
        ),
        "bolu" to CityTips(
            outdoor = listOf("Abant Gölü çevresinde yürü", "Yedigöller'e git", "Gölcük Tabiat Parkı'nda dolaş"),
            indoor = listOf("Mudurnu'nun tarihi evlerini gez"),
            taste = listOf("Mengen usulü bir yemek dene")
        ),
        "sakarya" to CityTips(
            outdoor = listOf("Sapanca Gölü kıyısında yürü", "Acarlar Longozu'nu gez"),
            indoor = listOf("Sakarya Müzesi'ne uğra"),
            taste = listOf("Islama köfte ye")
        ),
        "denizli" to CityTips(
            outdoor = listOf("Pamukkale travertenlerinde yürü", "Hierapolis'i gez"),
            indoor = listOf("Hierapolis Arkeoloji Müzesi'ni gez"),
            taste = listOf("Denizli kebabı dene")
        ),
        "aydin" to CityTips(
            outdoor = listOf("Kuşadası'nda sahil yürüyüşü", "Efes Antik Kenti'ni gez"),
            indoor = listOf("Aydın Arkeoloji Müzesi'ni gez"),
            taste = listOf("Taze incir tat")
        ),
        "balikesir" to CityTips(
            outdoor = listOf("Ayvalık'ta Şeytan Sofrası'nda gün batımını izle", "Cunda'da dolaş"),
            indoor = listOf("Ayvalık'ın tarihi sokaklarında bir kafeye otur"),
            taste = listOf("Ayvalık tostu ye", "Höşmerim tat")
        )
    )

    private val regions: Map<Region, List<String>> = mapOf(
        Region.MARMARA to listOf("istanbul", "edirne", "kirklareli", "tekirdag", "kocaeli", "sakarya", "yalova", "bursa", "bilecik", "balikesir", "canakkale"),
        Region.EGE to listOf("izmir", "manisa", "aydin", "denizli", "mugla", "usak", "kutahya", "afyonkarahisar"),
        Region.AKDENIZ to listOf("antalya", "isparta", "burdur", "mersin", "adana", "hatay", "osmaniye", "kahramanmaras"),
        Region.KARADENIZ to listOf("zonguldak", "bartin", "karabuk", "kastamonu", "sinop", "samsun", "ordu", "giresun", "trabzon", "rize", "artvin", "gumushane", "bayburt", "tokat", "amasya", "corum", "bolu", "duzce"),
        Region.IC_ANADOLU to listOf("ankara", "konya", "karaman", "aksaray", "nigde", "nevsehir", "kirsehir", "kirikkale", "yozgat", "kayseri", "sivas", "eskisehir", "cankiri"),
        Region.DOGU_ANADOLU to listOf("erzurum", "erzincan", "kars", "ardahan", "igdir", "agri", "van", "mus", "bitlis", "bingol", "elazig", "malatya", "tunceli", "hakkari"),
        Region.GUNEYDOGU to listOf("gaziantep", "kilis", "adiyaman", "sanliurfa", "diyarbakir", "mardin", "batman", "siirt", "sirnak")
    )

    private val regionTips: Map<Region, CityTips> = mapOf(
        Region.MARMARA to CityTips(listOf("Sahil boyunca yürü", "Bir şehir parkında bisiklet sür"), listOf("Şehrin müzesini gez"), listOf("Bir pastanede çay molası ver")),
        Region.EGE to CityTips(listOf("Zeytinlikler arasında yürüyüş", "Antik bir kenti gez"), listOf("Yerel bir müzeyi gez"), listOf("Zeytinyağlılarla dolu bir sofra kur")),
        Region.AKDENIZ to CityTips(listOf("Sahilde gün batımını izle", "Bir şelaleye git"), listOf("Arkeoloji müzesini gez"), listOf("Taze narenciye suyu iç")),
        Region.KARADENIZ to CityTips(listOf("Yeşilliklerin arasında yürüyüş", "Bir yaylaya çık"), listOf("Demli çay eşliğinde kitap oku"), listOf("Mısır ekmeği ve hamsi dene")),
        Region.IC_ANADOLU to CityTips(listOf("Şehrin tarihi merkezinde dolaş", "Bir göl kenarında yürü"), listOf("Şehrin müzesini gez"), listOf("Yöresel bir mantı dene")),
        Region.DOGU_ANADOLU to CityTips(listOf("Dağ manzarasına karşı yürüyüş", "Tarihi bir kaleye çık"), listOf("Sıcak bir mekânda semaver çayı iç"), listOf("Bol çeşitli bir kahvaltı yap")),
        Region.GUNEYDOGU to CityTips(listOf("Tarihi çarşıda dolaş", "Akşam serinliğinde bir yürüyüş"), listOf("Bir hanın avlusunda dinlen"), listOf("Mırra ya da menengiç kahvesi iç"))
    )

    fun provinceKey(place: Place): String = TurkishText.normalize(place.region ?: place.name)

    fun regionOf(place: Place): Region? {
        val key = provinceKey(place)
        return regions.entries.firstOrNull { key in it.value }?.key
    }

    fun tipsFor(place: Place): CityTips? =
        cities[provinceKey(place)] ?: regionOf(place)?.let { regionTips[it] }
}
