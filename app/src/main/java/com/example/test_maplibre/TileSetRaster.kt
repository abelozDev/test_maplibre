package com.example.test_maplibre

import com.google.gson.annotations.SerializedName


class TileSetRaster {
	var type: String? = null
	var url: String? = null
	var tileSize: Int? = null
	@SerializedName("volatile")
	var Volatile: Boolean? = null
}