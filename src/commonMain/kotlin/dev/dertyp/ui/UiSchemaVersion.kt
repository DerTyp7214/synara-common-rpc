package dev.dertyp.ui

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc

object UiSchemaVersion {
    const val NONE = 0

    const val CURRENT = 2

    const val HEADER = "X-Ui-Schema-Version"
}

@ModelDoc("The client's time zone, sent as an optional request header next to X-Ui-Schema-Version and Accept-Language. The server formats times on server-driven pages in this zone. Clients without it get times in UTC.")
object ClientTimeZone {
    @FieldDoc("Header name. The value is an IANA time zone id such as Europe/Berlin. Invalid values are ignored.")
    const val HEADER = "X-Time-Zone"
}
