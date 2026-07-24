with open("app/src/main/java/com/example/ui/screens/SequencerScreen.kt", "r") as f:
    lines = f.readlines()

out = []
skip = False
for line in lines:
    if "if (track.instrumentType == InstrumentType.SAMPLE)" in line:
        skip = True
    if skip and line.strip() == "))":
        pass
    if line.startswith("@Composable") and "fun TrackFxRackSection" in line:
        skip = False
    
    if not skip:
        out.append(line)

with open("app/src/main/java/com/example/ui/screens/SequencerScreen.kt", "w") as f:
    f.writelines(out)
