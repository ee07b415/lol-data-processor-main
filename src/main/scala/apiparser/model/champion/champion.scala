package apiparser.model.champion

import apiparser.model.BaseModel

// TODO: need flatten
class champion(
    val version: String,
    val id: String,
    val key: String,
    val name: String,
    val title: String,
    val blurb: String,
    val info: info,
    val image: image,
    val tags: List[String],
    val partype: String,
    val stats: stats
) extends BaseModel with Serializable {}
