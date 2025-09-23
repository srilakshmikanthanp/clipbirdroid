package com.srilakshmikanthanp.clipbirdroid.common.retrofit

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant

class InstantTypeAdapter : JsonDeserializer<Instant>, JsonSerializer<Instant> {
  override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Instant {
    return Instant.parse(json.asString)
  }

  override fun serialize(src: Instant, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
    return JsonPrimitive(src.toString())
  }
}
