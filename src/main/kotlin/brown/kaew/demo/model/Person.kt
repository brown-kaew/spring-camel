package brown.kaew.demo.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class Person(
    var name: String?,
    var favoriteColor: String?,
    var favoriteNumber: Int?,
) {
    constructor() : this(null, null, null)
}
