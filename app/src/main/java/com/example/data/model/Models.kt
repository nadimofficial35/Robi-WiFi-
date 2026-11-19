package com.example.data.model

data class UserProfile(
    val idNumber: String = "",
    val bdoName: String = "",
    val number: String = "",
    val password: String = "",
    val wic: String = "",
    val supervisor: String = ""
)

data class SrReport(
    val dateTime: String = "",
    val dateKey: String = "",
    val idNumber: String = "",
    val bdoName: String = "",
    val srId: String = "",
    val status: String = "",
    val nextDate: String = "",
    val comment: String = "",
    val supervisor: String = ""
)

enum class SyncStatus {
    LIVE,
    SYNCING,
    OFFLINE_CACHE
}

data class DeviceOption(
    val name: String,
    val unitPrice: Double
)

object InvoiceCatalog {
    val sl1Devices = listOf(
        DeviceOption("Basic device", 2499.0),
        DeviceOption("Plus Device", 3999.0),
        DeviceOption("Pro Device", 9999.0)
    )

    val sl2Packages = listOf(
        DeviceOption("Basic device", 1000.0),
        DeviceOption("Plus Device", 1288.0),
        DeviceOption("Pro Device", 1500.0)
    )
}

data class InvoiceData(
    val custName: String = "",
    val custEmail: String = "",
    val invNumber: String = "",
    val invDate: String = "",
    val desc1: String = "",
    val unit1: String = "0",
    val amt1: String = "0.00",
    val desc2: String = "",
    val unit2: String = "0",
    val amt2: String = "0.00",
    val desc3: String = "",
    val unit3: String = "0",
    val amt3: String = "0.00",
    val subtotal: String = "0.00",
    val total: String = "0.00"
)

data class QuizQuestion(
    val question: String,
    val options: List<String>
)

object RobiQuizBank {
    val questions = listOf(
        QuizQuestion(
            question = "Robi WiFi রাউটারের অ্যাডমিন প্যানেলে লগইন করার ডিফল্ট IP অ্যাড্রেস সাধারণত কোনটি?",
            options = listOf(
                "192.168.0.1 বা 192.168.1.1",
                "10.0.0.55",
                "172.16.254.1",
                "192.168.100.50"
            )
        ),
        QuizQuestion(
            question = "কাস্টমার পরবর্তীতে সংযোগ নিতে চাইলে Compline Report-এ কোন Status সিলেক্ট করতে হয়?",
            options = listOf(
                "Cancel",
                "Customer Unrech",
                "Customer Reschudul",
                "Ask Later"
            )
        ),
        QuizQuestion(
            question = "Robi WiFi Plus (T300 SE / K10) রাউটারে নির্দিষ্ট ওয়েবসাইট ব্লক করতে কোন ফিচার ব্যবহার করা হয়?",
            options = listOf(
                "Smart Restart",
                "SMS Center",
                "Parental Control / URL Filter",
                "WPS Mode"
            )
        ),
        QuizQuestion(
            question = "রাউটারের সিগন্যাল সবচেয়ে ভালো পাওয়ার জন্য রাউটারটি কোথায় রাখা উচিত?",
            options = listOf(
                "ঘরের এক কোণায় মেঝেতে",
                "খোলা জায়গায় ও জানালার পাশে উঁচু স্থানে",
                "আলমারির ভেতরে",
                "টিভি বা ফ্রিজের পেছনে"
            )
        ),
        QuizQuestion(
            question = "SR ID স্ট্যাটাস 'Customer Reschudul' দিলে নিচের কোন তথ্যটি দেওয়া বাধ্যতামূলক?",
            options = listOf(
                "Next Date",
                "Router MAC",
                "NID Number",
                "Alternative Email"
            )
        )
    )
}
