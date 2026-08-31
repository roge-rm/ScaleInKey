#!/usr/bin/env python3
"""Trim FluidR3_GM.sf2 down to just the presets ScaleInKey needs.

Hand-rolled SF2 (RIFF-based) parser/rebuilder -- no soundfont library available in the
environment this was written in. Walks phdr -> pbag -> pgen to find each wanted preset's
referenced instrument(s) (generator 41), then inst -> ibag -> igen to find each referenced sample
(generator 53), copies only those samples' PCM data (plus required padding) into a new smpl
chunk, and renumbers every cross-reference. Every kept sample is forced to independent mono
(sampleType=1, sampleLink=0) to avoid also having to track stereo-linked pairs.

Usage:
    python3 trim_sf2.py /path/to/FluidR3_GM.sf2 app/src/main/assets/scaleinkey_default.sf2

To add/change a bundled instrument, edit WANTED_PRESETS below (General MIDI bank/program
numbers) and re-run. Verify the output with verify_sf2.py before trusting it, then install a
debug build and check logcat for load errors (SoundEngine logs "Native engine failed to parse
soundfont" or "Failed to load bundled default soundfont" on failure) plus a real note/chord tap.

Original soundfont source (MIT License, Frank Wen): https://member.keymusician.com/Member/FluidR3_GM/
(mirrored at e.g. https://raw.githubusercontent.com/urish/cinto/master/media/FluidR3%20GM.sf2 as of
2026 -- ~148MB, verify the RIFF/"sfbk"/"Fluid R3 GM" header before trusting any mirror).
"""
import struct
import sys

IN_PATH = sys.argv[1]
OUT_PATH = sys.argv[2]
# (bank, program) -> label, purely for logging. Program numbers are General MIDI (0-indexed).
WANTED_PRESETS = {
    (0, 0): "Acoustic Grand Piano",
    (0, 24): "Nylon String Guitar",
    (0, 32): "Acoustic Bass",
    (0, 105): "Banjo",
}

PHDR_FMT = "<20sHHHIII"
PHDR_SIZE = struct.calcsize(PHDR_FMT)
BAG_FMT = "<HH"
BAG_SIZE = struct.calcsize(BAG_FMT)
MOD_FMT = "<HHhHH"
MOD_SIZE = struct.calcsize(MOD_FMT)
GEN_FMT = "<HH"
GEN_SIZE = struct.calcsize(GEN_FMT)
INST_FMT = "<20sH"
INST_SIZE = struct.calcsize(INST_FMT)
SHDR_FMT = "<20sIIIIIBbHH"
SHDR_SIZE = struct.calcsize(SHDR_FMT)

GEN_OP_INSTRUMENT = 41
GEN_OP_SAMPLE_ID = 53


def read_riff_subchunks(data, start, end):
    """Yield (chunk_id, data_offset, data_len) for each direct subchunk in [start, end)."""
    pos = start
    while pos < end:
        chunk_id = data[pos:pos + 4].decode("ascii")
        chunk_size = struct.unpack_from("<I", data, pos + 4)[0]
        data_start = pos + 8
        yield chunk_id, data_start, chunk_size
        pos = data_start + chunk_size
        if pos % 2 == 1:
            pos += 1
    assert pos == end, f"chunk walk overran: pos={pos} end={end}"


def parse_list_chunk(data, offset, size):
    """LIST chunk: 4-byte listType then subchunks. Returns (listType, {id: (off,len)})."""
    list_type = data[offset:offset + 4].decode("ascii")
    subchunks = {}
    for cid, off, length in read_riff_subchunks(data, offset + 4, offset + size):
        subchunks[cid] = (off, length)
    return list_type, subchunks


def parse_records(data, offset, length, fmt, size):
    assert length % size == 0, f"{fmt} region not a multiple of record size"
    count = length // size
    return [struct.unpack_from(fmt, data, offset + i * size) for i in range(count)]


def main():
    with open(IN_PATH, "rb") as f:
        data = f.read()

    assert data[0:4] == b"RIFF"
    riff_size = struct.unpack_from("<I", data, 4)[0]
    assert data[8:12] == b"sfbk"
    top_chunks = {}
    for cid, off, length in read_riff_subchunks(data, 12, 12 + riff_size - 4):
        assert cid == "LIST"
        list_type, subs = parse_list_chunk(data, off, length)
        top_chunks[list_type] = (off, length, subs)

    info_off, info_len, _ = top_chunks["INFO"]
    sdta_off, sdta_len, sdta_subs = top_chunks["sdta"]
    pdta_off, pdta_len, pdta_subs = top_chunks["pdta"]

    smpl_off, smpl_len = sdta_subs["smpl"]
    print(f"smpl: {smpl_len} bytes ({smpl_len // 2} samples)")

    phdr = parse_records(data, *pdta_subs["phdr"], PHDR_FMT, PHDR_SIZE)
    pbag = parse_records(data, *pdta_subs["pbag"], BAG_FMT, BAG_SIZE)
    pmod = parse_records(data, *pdta_subs["pmod"], MOD_FMT, MOD_SIZE)
    pgen = parse_records(data, *pdta_subs["pgen"], GEN_FMT, GEN_SIZE)
    inst = parse_records(data, *pdta_subs["inst"], INST_FMT, INST_SIZE)
    ibag = parse_records(data, *pdta_subs["ibag"], BAG_FMT, BAG_SIZE)
    imod = parse_records(data, *pdta_subs["imod"], MOD_FMT, MOD_SIZE)
    igen = parse_records(data, *pdta_subs["igen"], GEN_FMT, GEN_SIZE)
    shdr = parse_records(data, *pdta_subs["shdr"], SHDR_FMT, SHDR_SIZE)

    print(f"phdr={len(phdr)} pbag={len(pbag)} pgen={len(pgen)} inst={len(inst)} "
          f"ibag={len(ibag)} igen={len(igen)} shdr={len(shdr)}")

    # phdr/inst arrays each end with a required terminal sentinel record; real count excludes it.
    num_real_presets = len(phdr) - 1
    num_real_insts = len(inst) - 1

    def preset_zone_range(p_idx):
        return phdr[p_idx][3], phdr[p_idx + 1][3]  # wPresetBagNdx of this / next record

    def gen_range_for_bag(bag_list, bag_idx):
        return bag_list[bag_idx][0], bag_list[bag_idx + 1][0]  # wGenNdx of this / next bag

    def mod_range_for_bag(bag_list, bag_idx):
        return bag_list[bag_idx][1], bag_list[bag_idx + 1][1]

    def inst_zone_range(i_idx):
        return inst[i_idx][1], inst[i_idx + 1][1]  # wInstBagNdx

    # --- Find wanted presets, and every instrument they reference. ---
    wanted_preset_indices = []  # in a fixed, deterministic order
    for p_idx in range(num_real_presets):
        name, program, bank, *_ = phdr[p_idx]
        key = (bank, program)
        if key in WANTED_PRESETS:
            wanted_preset_indices.append(p_idx)
            print(f"found preset bank={bank} program={program} name={name.split(b'\\x00')[0]!r}")
    assert len(wanted_preset_indices) == len(WANTED_PRESETS), (
        f"expected {len(WANTED_PRESETS)} presets, found {len(wanted_preset_indices)}"
    )

    wanted_inst_indices = []
    seen_insts = set()
    for p_idx in wanted_preset_indices:
        z_start, z_end = preset_zone_range(p_idx)
        for z in range(z_start, z_end):
            g_start, g_end = gen_range_for_bag(pbag, z)
            for g in range(g_start, g_end):
                oper, amount = pgen[g]
                if oper == GEN_OP_INSTRUMENT:
                    if amount not in seen_insts:
                        seen_insts.add(amount)
                        wanted_inst_indices.append(amount)
    print(f"referenced instruments: {len(wanted_inst_indices)}")

    wanted_sample_indices = []
    seen_samples = set()
    for i_idx in wanted_inst_indices:
        z_start, z_end = inst_zone_range(i_idx)
        for z in range(z_start, z_end):
            g_start, g_end = gen_range_for_bag(ibag, z)
            for g in range(g_start, g_end):
                oper, amount = igen[g]
                if oper == GEN_OP_SAMPLE_ID:
                    if amount not in seen_samples:
                        seen_samples.add(amount)
                        wanted_sample_indices.append(amount)
    print(f"referenced samples: {len(wanted_sample_indices)}")

    # --- Build new smpl data + shdr, remapping old sample index -> new sample index. ---
    PAD_SAMPLES = 46
    new_smpl = bytearray()
    new_shdr = []
    old_to_new_sample = {}
    for new_idx, old_idx in enumerate(wanted_sample_indices):
        name, s_start, s_end, s_loopstart, s_loopend, rate, orig_pitch, pitch_corr, link, stype = shdr[old_idx]
        byte_start = s_start * 2
        byte_end = s_end * 2
        sample_bytes = data[smpl_off + byte_start: smpl_off + byte_end]

        new_start = len(new_smpl) // 2
        new_smpl.extend(sample_bytes)
        new_smpl.extend(b"\x00\x00" * PAD_SAMPLES)
        new_end = new_start + (s_end - s_start)
        new_loopstart = new_start + (s_loopstart - s_start)
        new_loopend = new_start + (s_loopend - s_start)

        # Force independent mono regardless of original stereo linkage (see module docstring).
        new_shdr.append((name, new_start, new_end, new_loopstart, new_loopend, rate,
                          orig_pitch, pitch_corr, 0, 1))
        old_to_new_sample[old_idx] = new_idx
    # Terminal sentinel sample record.
    new_shdr.append((b"EOS" + b"\x00" * 17, 0, 0, 0, 0, 0, 0, 0, 0, 0))

    # --- Rebuild instrument generators/bags, remapping sampleID (53) values. ---
    new_igen = []
    new_ibag = []
    new_imod = []
    old_to_new_inst = {}
    for new_i_idx, old_i_idx in enumerate(wanted_inst_indices):
        name, _ = inst[old_i_idx]
        z_start, z_end = inst_zone_range(old_i_idx)
        for z in range(z_start, z_end):
            new_ibag.append((len(new_igen), len(new_imod)))
            g_start, g_end = gen_range_for_bag(ibag, z)
            for g in range(g_start, g_end):
                oper, amount = igen[g]
                if oper == GEN_OP_SAMPLE_ID:
                    amount = old_to_new_sample[amount]
                new_igen.append((oper, amount))
            m_start, m_end = mod_range_for_bag(ibag, z)
            for m in range(m_start, m_end):
                new_imod.append(imod[m])
        old_to_new_inst[old_i_idx] = new_i_idx
    new_ibag.append((len(new_igen), len(new_imod)))  # terminal sentinel
    new_igen.append((0, 0))
    new_imod.append((0, 0, 0, 0, 0))

    new_inst = []
    for old_i_idx in wanted_inst_indices:
        name, _ = inst[old_i_idx]
        # wInstBagNdx recomputed below once we know each instrument's zone count; do a second
        # pass since ibag entries were appended per-instrument above in the same order.
        new_inst.append(name)
    # Recompute each instrument's starting ibag index from the zone counts we just emitted.
    inst_bag_starts = []
    cursor = 0
    for old_i_idx in wanted_inst_indices:
        z_start, z_end = inst_zone_range(old_i_idx)
        inst_bag_starts.append(cursor)
        cursor += (z_end - z_start)
    assert cursor == len(new_ibag) - 1
    new_inst_records = [(name, start) for name, start in zip(new_inst, inst_bag_starts)]
    new_inst_records.append((b"EOI" + b"\x00" * 17, cursor))

    # --- Rebuild preset generators/bags, remapping instrument (41) values. ---
    new_pgen = []
    new_pbag = []
    new_pmod = []
    preset_bag_starts = []
    for old_p_idx in wanted_preset_indices:
        z_start, z_end = preset_zone_range(old_p_idx)
        preset_bag_starts.append(len(new_pbag))
        for z in range(z_start, z_end):
            new_pbag.append((len(new_pgen), len(new_pmod)))
            g_start, g_end = gen_range_for_bag(pbag, z)
            for g in range(g_start, g_end):
                oper, amount = pgen[g]
                if oper == GEN_OP_INSTRUMENT:
                    amount = old_to_new_inst[amount]
                new_pgen.append((oper, amount))
            m_start, m_end = mod_range_for_bag(pbag, z)
            for m in range(m_start, m_end):
                new_pmod.append(pmod[m])
    new_pbag.append((len(new_pgen), len(new_pmod)))  # terminal sentinel
    new_pgen.append((0, 0))
    new_pmod.append((0, 0, 0, 0, 0))

    new_phdr = []
    for new_p_idx, old_p_idx in enumerate(wanted_preset_indices):
        name, program, bank, _bagndx, library, genre, morphology = phdr[old_p_idx]
        new_phdr.append((name, program, bank, preset_bag_starts[new_p_idx], library, genre, morphology))
    new_phdr.append((b"EOP" + b"\x00" * 17, 0, 0, len(new_pbag) - 1, 0, 0, 0))

    # --- Serialize everything back out. ---
    def pack_records(records, fmt):
        return b"".join(struct.pack(fmt, *r) for r in records)

    def riff_chunk(chunk_id, payload):
        assert len(chunk_id) == 4
        out = chunk_id.encode("ascii") + struct.pack("<I", len(payload)) + payload
        if len(payload) % 2 == 1:
            out += b"\x00"
        return out

    smpl_chunk = riff_chunk("smpl", bytes(new_smpl))
    sdta_payload = b"sdta" + smpl_chunk
    sdta_list = riff_chunk("LIST", sdta_payload)

    pdta_payload = b"pdta"
    pdta_payload += riff_chunk("phdr", pack_records(new_phdr, PHDR_FMT))
    pdta_payload += riff_chunk("pbag", pack_records(new_pbag, BAG_FMT))
    pdta_payload += riff_chunk("pmod", pack_records(new_pmod, MOD_FMT))
    pdta_payload += riff_chunk("pgen", pack_records(new_pgen, GEN_FMT))
    pdta_payload += riff_chunk("inst", pack_records(new_inst_records, INST_FMT))
    pdta_payload += riff_chunk("ibag", pack_records(new_ibag, BAG_FMT))
    pdta_payload += riff_chunk("imod", pack_records(new_imod, MOD_FMT))
    pdta_payload += riff_chunk("igen", pack_records(new_igen, GEN_FMT))
    pdta_payload += riff_chunk("shdr", pack_records(new_shdr, SHDR_FMT))
    pdta_list = riff_chunk("LIST", pdta_payload)

    # Copy the original INFO LIST verbatim (small, harmless metadata).
    info_list = data[info_off - 8: info_off + info_len]

    sfbk_payload = b"sfbk" + info_list + sdta_list + pdta_list
    out_data = b"RIFF" + struct.pack("<I", len(sfbk_payload)) + sfbk_payload

    with open(OUT_PATH, "wb") as f:
        f.write(out_data)

    print(f"wrote {OUT_PATH}: {len(out_data)} bytes")
    print(f"kept {len(wanted_sample_indices)} samples, {len(wanted_inst_indices)} instruments, "
          f"{len(wanted_preset_indices)} presets")


if __name__ == "__main__":
    main()
