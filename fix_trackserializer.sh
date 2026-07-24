sed -i 's/val filterCutoff: Float = 1.0f/val filterCutoff: Float = 1.0f,\n    val customSamplePath: String? = null/' app/src/main/java/com/example/utils/TrackSerializer.kt
sed -i 's/filterCutoff = track.filterCutoff/filterCutoff = track.filterCutoff,\n                customSamplePath = track.customSamplePath/' app/src/main/java/com/example/utils/TrackSerializer.kt
sed -i 's/filterCutoff = item.filterCutoff/filterCutoff = item.filterCutoff,\n                    customSamplePath = item.customSamplePath/' app/src/main/java/com/example/utils/TrackSerializer.kt
