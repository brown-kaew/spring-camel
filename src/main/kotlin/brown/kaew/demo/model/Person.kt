package brown.kaew.demo.model

data class Person(
    var name: String,
    var favoriteColor: String,
    var favoriteNumber: Int,
) {
    constructor() : this("", "", 0)
}
