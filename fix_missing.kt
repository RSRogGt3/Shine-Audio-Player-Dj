                }
            }
        }
    }
}

@Composable
fun TrackFxRackSection(
    track: SequencerTrack,
    onDistortionChange: (Float) -> Unit,
    onDelayChange: (Float) -> Unit,
    onReverbChange: (Float) -> Unit,
    onFilterChange: (Float) -> Unit,
    onToggleBassKill: () -> Unit,
    onToggleMidKill: () -> Unit,
    onToggleHighKill: () -> Unit,
    onResetFx: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10101A)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ECHTZEIT AUDIO-FX RACK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Distortion", color = Color.Gray, fontSize = 10.sp)
                    Slider(value = track.distortion, onValueChange = onDistortionChange, valueRange = 0f..1f)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Delay", color = Color.Gray, fontSize = 10.sp)
                    Slider(value = track.delayMix, onValueChange = onDelayChange, valueRange = 0f..1f)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reverb", color = Color.Gray, fontSize = 10.sp)
                    Slider(value = track.reverbMix, onValueChange = onReverbChange, valueRange = 0f..1f)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Filter Cutoff", color = Color.Gray, fontSize = 10.sp)
                    Slider(value = track.filterCutoff, onValueChange = onFilterChange, valueRange = 0f..1f)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(selected = track.isBassKilled, onClick = onToggleBassKill, label = { Text("BASS KILL") })
                FilterChip(selected = track.isMidKilled, onClick = onToggleMidKill, label = { Text("MID KILL") })
                FilterChip(selected = track.isHighKilled, onClick = onToggleHighKill, label = { Text("HI KILL") })
                Button(onClick = onResetFx, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("RESET") }
            }
        }
    }
}

@Composable
fun AddTrackModal(onDismiss: () -> Unit, onSelectInstrument: (InstrumentType) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Instrument auswählen") },
        text = {
            LazyColumn {
                items(InstrumentType.values()) { inst ->
                    TextButton(onClick = { onSelectInstrument(inst); onDismiss() }) {
                        Text("${inst.icon} ${inst.displayName}")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveProjectModal(initialName: String, onDismiss: () -> Unit, onSave: (String, Boolean) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Projekt speichern") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }) },
        confirmButton = { TextButton(onClick = { onSave(name, false) }) { Text("Speichern") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
fun LoadProjectsModal(
    projects: List<SequencerProjectEntity>,
    onDismiss: () -> Unit,
    onLoadProject: (SequencerProjectEntity) -> Unit,
    onDeleteProject: (String) -> Unit,
    onSyncCloud: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Projekte laden") },
        text = {
            LazyColumn {
                items(projects) { proj ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onLoadProject(proj) }) {
                        Text(proj.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDeleteProject(proj.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}

@Composable
fun AiBeatGeneratorModal(isGenerating: Boolean, onDismiss: () -> Unit, onGenerate: (String) -> Unit) {
    var prompt by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("KI Beat Generator") },
        text = { OutlinedTextField(value = prompt, onValueChange = { prompt = it }, label = { Text("Prompt (z.B. Techno 130 BPM)") }) },
        confirmButton = { TextButton(onClick = { onGenerate(prompt) }, enabled = !isGenerating) { Text("Generieren") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
fun TrackWaveformPreview(track: SequencerTrack, modifier: Modifier = Modifier, barCount: Int = 32, showDetails: Boolean = false, currentStep: Int = -1) {
    // Dummy implementation to replace the missing one
    Box(modifier = modifier.height(40.dp).background(Color.DarkGray))
}
