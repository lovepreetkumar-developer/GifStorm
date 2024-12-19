package com.techfathers.gifstorm.models.models_to_send

/*
//q:excited
key:TIN0V3M60QNO
locale:en_US
contentfilter:off
//ar_range:all
limit:50
//pos:50
//anon_id:n/a
media_filter:minimal
*/

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

data class GetGifsModel(
    var q: String?,
    var key: String,
    var locale: String,
    var contentfilter: String,
    var ar_range: String?,
    var limit: Int?,
    var pos: Int?,
    var anon_id: String?,
    var media_filter: String?,
){
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        null,
        null,
        "",
        ""
    )
}