package dev.dertyp.rpc.rest.client

class RestException(val status: Int, message: String) : RuntimeException(message)
