package brown.kaew.demo.model

data class Person(
    var name: String?,
    var favoriteColor: String?,
    var favoriteNumber: Int?,
    var favoriteFood: String? = "Pizza",
) {
    constructor() : this(null, null, null)
}
