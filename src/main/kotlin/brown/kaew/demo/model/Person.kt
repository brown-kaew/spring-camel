package brown.kaew.demo.model

import org.apache.avro.reflect.AvroDefault

data class Person(
    var name: String?,
    var favoriteColor: String?,
    var favoriteNumber: Int?,
    @AvroDefault(value = "\"Mama\"")
    var favoriteFood: String? = "Pizza"
) {
    constructor() : this(null, null, null)
}
