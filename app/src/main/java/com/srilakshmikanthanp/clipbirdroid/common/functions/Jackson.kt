package com.srilakshmikanthanp.clipbirdroid.common.functions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun jacksonObjectMapperWithTimeModule(): ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
