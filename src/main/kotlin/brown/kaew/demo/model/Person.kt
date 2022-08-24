package brown.kaew.demo.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(
    var name: String?,
    var favoriteColor: String?,
    var favoriteNumber: Int?,
    var favoriteFood: String? = "Pizza"
) {
    constructor() : this(null, null, null)
}
