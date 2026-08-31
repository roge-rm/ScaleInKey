package com.rm.scaleinkey.music

/**
 * One canonical letter+accidental spelling per chromatic pitch class (0..11),
 * used to drive the root picker. Flats on black keys except F# (idiomatic default).
 */
val CANONICAL_ROOTS: List<Note> = listOf(
    Note(Letter.C, 0),  // 0  C
    Note(Letter.D, -1), // 1  Db
    Note(Letter.D, 0),  // 2  D
    Note(Letter.E, -1), // 3  Eb
    Note(Letter.E, 0),  // 4  E
    Note(Letter.F, 0),  // 5  F
    Note(Letter.F, 1),  // 6  F#
    Note(Letter.G, 0),  // 7  G
    Note(Letter.A, -1), // 8  Ab
    Note(Letter.A, 0),  // 9  A
    Note(Letter.B, -1), // 10 Bb
    Note(Letter.B, 0),  // 11 B
)
