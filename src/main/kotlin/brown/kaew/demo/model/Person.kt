package brown.kaew.demo.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.avro.reflect.AvroDefault

data class Person(
    var name: String?,
    var favoriteColor: String?,
    var favoriteNumber: Int?,
//    @AvroDefault(value = "\"Mama\"")
    @JsonProperty(defaultValue = "\"Mama\"")
    var favoriteFood: String? = "Pizza",
    var pets: List<Pet> = arrayListOf(Pet("doggy"))
) {
    constructor() : this(null, null, null)
}
