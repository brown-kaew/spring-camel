package brown.kaew.demo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Pet(
    var name: String? = null,
//    @JsonProperty(defaultValue = "\"black\"")
//    var color: String? = "red",
)
