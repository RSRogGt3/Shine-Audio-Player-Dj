sed -i 's/InstrumentType.SYNTH, InstrumentType.PERC, InstrumentType.FX/InstrumentType.SYNTH, InstrumentType.PERC, InstrumentType.FX, InstrumentType.SAMPLE/g' app/src/main/java/com/example/ui/viewmodels/SequencerViewModel.kt
sed -i 's/InstrumentType.PERC, InstrumentType.FX ->/InstrumentType.PERC, InstrumentType.FX, InstrumentType.SAMPLE ->/g' app/src/main/java/com/example/ui/viewmodels/SequencerViewModel.kt
sed -i 's/InstrumentType.SYNTH, InstrumentType.FX ->/InstrumentType.SYNTH, InstrumentType.FX, InstrumentType.SAMPLE ->/g' app/src/main/java/com/example/ui/viewmodels/SequencerViewModel.kt
