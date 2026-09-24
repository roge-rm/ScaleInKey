# ScaleInKey

ScaleInKey is an Android app that shows you the notes and chords in a scale. Pick a root and a scale and it lays the notes out on a piano, guitar, ukulele or bass, along with the chords that go with it.

There are 33 scales to choose from, from the regular modes to blues, jazz and some of the more exotic ones. Tap any note or chord and you'll hear it played through a real soundfont, and you can load your own if you don't like the default one. There's also a simple chord progression sequencer, so you can string some chords together and hear how they sound, with looping and adjustable BPM.

Requires Android 8.0 or higher.

Enjoy!
Dan

<img src="docs/screenshot-main.png" alt="ScaleInKey main screen: C Ionian scale with diatonic chords and the Guitar fingering-chart diagram for the I chord" width="200" /> <img src="docs/screenshot-sequencer.png" alt="ScaleInKey chord progression sequencer screen with the diatonic chord palette and playback controls" width="200" />

## Features

- 33 scales in six groups, all spelled properly in every key:
  - Modes: Ionian (Major), Dorian, Phrygian, Lydian, Mixolydian, Aeolian (Natural Minor) and
    Locrian.
  - Minor variants: Harmonic Minor and Melodic Minor.
  - Pentatonic & Blues: Major and Minor Pentatonic, Major and Minor Blues, and Egyptian
    Pentatonic.
  - World & Exotic: Hungarian Minor, Byzantine, Persian, Hirajoshi, In Sen, Iwato, Enigmatic,
    Phrygian Dominant, Neapolitan Minor and Neapolitan Major.
  - Jazz: Altered, Lydian Dominant, Lydian Augmented, Locrian ♮2 (Half-Diminished), Bebop
    Dominant and Bebop Major.
  - Symmetric: Whole Tone, Diminished Whole-Half and Diminished Half-Whole.
- The chords in the key for every scale that has them. They show as triads, and you can long-press
  any chord to switch it to its 7th chord. That includes the less common ones you get from harmonic
  and melodic minor, like minor-major 7th, augmented-major 7th and diminished 7th.
- Diagrams for piano, guitar, ukulele and bass that highlight the scale, or just the chord if
  you've picked one. Bass can be switched between 4 and 5 strings.
- Guitar, ukulele and bass can also show a chord chart instead of the whole neck. When a chord is
  picked you get a chord box with the fingering. Common chords use the shapes people actually play,
  and everything else gets worked out near the nut so even the odd chords have something you can
  play. With no chord picked it shows a box for the scale in one position. Bass just shows where
  the root is, since you don't really strum chords on a bass.
- Tap any note, chord, fret or chart to hear it. The sound comes from a real SF2 synth, not a
  beep. A small soundfont comes built in, and you can load your own `.sf2` file if you'd rather.
  Whatever you tap lights up in its own colour so you can see exactly what you hit.
- A chord progression sequencer on its own screen (tap the 🎹 next to the title). Tap chords to add
  them to the progression, long-press one first to add it as a 7th, drag them around to change the
  order and tap one to take it out. Set the BPM (120 to start), how many beats each chord gets, and
  whether it loops.

## Installing

The easiest way to install ScaleInKey and keep it up to date is through my F-Droid repo, which has
my other apps too. With the [F-Droid](https://f-droid.org/) app installed, open this link on
your phone to add the repo, or scan the QR code on the [repo page](https://roge-rm.gitlab.io/repo/):

[https://roge-rm.gitlab.io/repo](https://roge-rm.gitlab.io/repo?fingerprint=80438B253C257BCCE05CDCB9E3AC9B6174C2250659962B14FCBE7F32FD42D53E)

Then search for ScaleInKey in F-Droid. When a new version comes out, F-Droid will offer it as an update.

You can also download the APK from the [Releases](https://github.com/roge-rm/ScaleInKey/releases)
page and sideload it. Both are signed with the same key, so you can switch between them without
reinstalling.

## Requirements

- Android Studio (recent stable) with the NDK and CMake installed, since the sound engine is
  written in C++.
- A JDK. Android Studio has one built in, but if you're running Gradle from the command line you'll
  need to point `JAVA_HOME` at it (or any other JDK), e.g.:
  ```
  export JAVA_HOME=/path/to/android-studio/jbr
  ```
- minSdk 26 / targetSdk 37.

## Building & testing

```
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run the unit tests
```

The unit tests cover the music theory and the chord fingering, and they don't need a phone or the
emulator.

## Architecture

- `music/` - all the music theory, in plain Kotlin with no UI code: building and spelling scales
  and chords, naming chord qualities, instrument tunings, and the chord fingering search and its
  table of known shapes. It's tested pretty heavily, including brute-force checks over every scale,
  root and degree.
- `ui/` - the Jetpack Compose screens and components. The diagrams (`ui/components/diagrams/`) are
  drawn on a Canvas instead of using images, so they can highlight notes and label fingers on the
  fly.
- `audio/` and `cpp/` - the sound engine (`native_sound_engine.cpp`), built on Oboe and
  TinySoundFont. Notes from the Kotlin side go through a lock-free queue so they never trip over
  the audio thread while it's playing.

## Attribution

- The built-in soundfont (`app/src/main/assets/scaleinkey_default.sf2`) is a trimmed down copy of
  Frank Wen's **FluidR3 GM** (MIT License). See the `.NOTICE.txt` next to it for details.
- The sound engine includes **TinySoundFont** (`cpp/tsf.h`, MIT License). See
  `cpp/tsf.h.NOTICE.txt`.

## License

MIT, see [LICENSE](LICENSE).
