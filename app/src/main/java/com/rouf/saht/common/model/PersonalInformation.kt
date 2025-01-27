package com.rouf.saht.common.model

data class PersonalInformation (
    var name: String,
    var gender: Gender,

    var height: String,
    var heightUnit: String,

    var weight: String,
    var weightUnit: String,

    var selectedYear: Int,
    var selectedMonth: Int,
    var selectedDay: Int,
    var formatedDate: String,
    var age: String
)