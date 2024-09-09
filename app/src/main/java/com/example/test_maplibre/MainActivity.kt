package com.example.test_maplibre

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.test_maplibre.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.mapbox.android.gestures.StandardGestureDetector
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.building.BuildingPlugin
import org.maplibre.android.plugins.scalebar.ScaleBarOptions
import org.maplibre.android.plugins.scalebar.ScaleBarPlugin
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import org.maplibre.android.style.sources.VectorSource
import kotlin.math.ln

class MainActivity : AppCompatActivity(){

	private lateinit var binding: ActivityMainBinding
	private lateinit var mapView: MapView
	val osmUrl = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"
	val terrain = "https://mt1.google.com/vt/lyrs=p&x={x}&y={y}&z={z}"
	val google = "https://mt0.google.com/vt/lyrs=y&hl=ru&x={x}&y={y}&z={z}"
	private var buildingPlugin: BuildingPlugin? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		MapLibre.getInstance(this)
		MapLibre.setConnected(true)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.fab.setOnClickListener {
			addOpenStreetMapRasterLayer()
		}
		binding.tilt.setOnClickListener{
			add3DView()
		}

		mapView = binding.root.findViewById(R.id.mapView)
		val mapbox = "mapbox://styles/mapbox/standard"
		val default = "https://demotiles.maplibre.org/style.json"
		val street = "https://api.maptiler.com/maps/streets-v2/style.json?key=XrspMiCeiNBCUzWyzqQx"


		binding.mapView.getMapAsync { map ->
			val st = Style.Builder().fromUri(street)
			map.setStyle(st){ s ->
				buildingPlugin = BuildingPlugin(binding.mapView, map, s)
				buildingPlugin?.setColor(Color.RED)
				buildingPlugin?.setMinZoomLevel(15f);
				buildingPlugin?.setVisibility(true);
			}
			map.cameraPosition = CameraPosition.Builder()
				.target(LatLng(44.0, 34.0))
				.tilt(0.0)
				.zoom(4.0)
				.build()

			addScalebar(map)
			map.gesturesManager.setStandardGestureListener(object :
				StandardGestureDetector.StandardOnGestureListener {
				override fun onDown(p0: MotionEvent): Boolean {
					return false
				}

				override fun onShowPress(p0: MotionEvent) {
				}

				override fun onSingleTapUp(p0: MotionEvent): Boolean {
					return false
				}

				override fun onScroll(
					p0: MotionEvent?,
					p1: MotionEvent,
					p2: Float,
					p3: Float
				): Boolean {
					val target = map.cameraPosition.target ?: return false
					val lat = target.latitude.toString()
					val lng = target.longitude.toString()
					if (lat.length <9 || lng.length <9) return false
					val text = "Координаты:\nШ: ${lat.substring(0,8)},Д: ${lng.substring(0,8)}"
					binding.textCoord.text = text
					return false
				}

				override fun onLongPress(p0: MotionEvent) {
					return
				}

				override fun onFling(
					p0: MotionEvent?,
					p1: MotionEvent,
					p2: Float,
					p3: Float
				): Boolean {
					return false
				}

				override fun onSingleTapConfirmed(p0: MotionEvent): Boolean {
					return false
				}

				override fun onDoubleTap(p0: MotionEvent): Boolean {
					return false
				}

				override fun onDoubleTapEvent(p0: MotionEvent): Boolean {
					return false
				}

			})
		}

	}


	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onStart() {
		super.onStart()
		binding.mapView.onStart()
	}

	override fun onResume() {
		super.onResume()
		binding.mapView.onResume()
	}

	override fun onPause() {
		super.onPause()
		binding.mapView.onPause()
	}

	override fun onStop() {
		super.onStop()
		binding.mapView.onStop()
	}

	override fun onLowMemory() {
		super.onLowMemory()
		binding.mapView.onLowMemory()
	}

	override fun onDestroy() {
		super.onDestroy()
		binding.mapView.onDestroy()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		binding.mapView.onSaveInstanceState(outState)
	}
	private val nameVSource = "osm-vector-source"
	private val nameVLayer = "osm-vector-layer"
	val vectorHorizontal = "https://api.maptiler.com/tiles/contours-v2/tiles.json?key=XrspMiCeiNBCUzWyzqQx"
	private fun addOpenStreetMapVectorLayer() {
		binding.mapView.getMapAsync { map ->
			map.style?.let {
				val vectorSource = VectorSource(
					nameVSource,
					vectorHorizontal
				)
				it.removeLayer(nameVLayer)
				it.removeSource(nameVSource)
				val terrainData = LineLayer(nameVLayer, nameVSource)
				terrainData.setProperties(
					lineJoin(Property.LINE_JOIN_ROUND),
					lineCap(Property.LINE_CAP_ROUND),
					lineColor(Color.parseColor("#ff69b4")),
					lineWidth(1.9f)
				)
				it.addSource(vectorSource)
				it.addLayer(terrainData)
			}
		}
	}
		private val nameSource = "osm-raster-source"
	private val nameLayer = "osm-raster-layer"
	private var isGoogle = true

	private fun addOpenStreetMapRasterLayer() {
		binding.mapView.getMapAsync { map ->
			map.style?.let {
				val url = if (isGoogle) google else terrain
				isGoogle = !isGoogle

				val raster = TileSetRaster()
				raster.url = url
				raster.Volatile = true
				raster.type = "raster"
				val tileset = TileSet(Gson().toJson(raster), url)

				it.removeLayer(nameLayer)
				it.removeSource(nameSource)
				if (isGoogle) return@getMapAsync
				val osmRasterSource = RasterSource(nameSource, tileset, 256)
				val osmRasterLayer = RasterLayer(nameLayer, nameSource)
				it.addSource(osmRasterSource)
				it.addLayer(osmRasterLayer)
			}
		}
	}

	private var is3d = true

	private fun add3DView() {
		binding.mapView.getMapAsync{map ->
			val tilt = if (is3d) 60.0 else 0.0
			is3d = !is3d
			val geoPoint = map.cameraPosition.target
			val zoom = map.cameraPosition.zoom
			map.cameraPosition = CameraPosition.Builder()
				.target(geoPoint)
				.zoom(zoom)
				.tilt(tilt)
				.build()
		}
	}

	private fun addScalebar(maplibreMap: MapLibreMap) {
		val scaleBarPlugin =
			ScaleBarPlugin(binding.mapView, maplibreMap)
		val scaleBarOptions = ScaleBarOptions(this)
		scaleBarOptions
			.setTextColor(android.R.color.black)
			.setTextSize(40f)
			.setBarHeight(5f)
			.setBorderWidth(2f)
			.setRefreshInterval(15)
			.setMarginTop(15f)
			.setMarginLeft(16f)
			.setTextBarMargin(15f)
			.setMaxWidthRatio(0.5f)
			.setShowTextBorder(true)
			.setTextBorderWidth(5f)

		scaleBarPlugin.create(scaleBarOptions)
	}

	/*private fun setupTestLine() {
		binding.mapView.getMapAsync { map ->
			val style = map.style
			val source = GeoJsonSource("source-id")
			val lineLayer = LineLayer("layer-id", source.id)
			val startPoint: Point = Point.fromLngLat(-122.447244, 37.769145)
			val endPoint: Point = Point.fromLngLat(-50.447244, 27.769145)
			val pointList: List<Point> = listOf(startPoint, endPoint)
			source.setGeoJson(LineString.fromLngLats(pointList))
			style?.addSource(source)
			style?.addLayer(lineLayer)
		}

	}*/
}
