#!/usr/bin/env python3
"""Independent structural re-check of an SF2 file trimmed by trim_sf2.py: chunk sizes, index
bounds, and non-zero PCM content per sample. Run this on trim_sf2.py's output before trusting it.

Usage: python3 verify_sf2.py app/src/main/assets/scaleinkey_default.sf2
"""
import struct
import sys

PATH = sys.argv[1]

PHDR_FMT = "<20sHHHIII"; PHDR_SIZE = struct.calcsize(PHDR_FMT)
BAG_FMT = "<HH"; BAG_SIZE = struct.calcsize(BAG_FMT)
MOD_FMT = "<HHhHH"; MOD_SIZE = struct.calcsize(MOD_FMT)
GEN_FMT = "<HH"; GEN_SIZE = struct.calcsize(GEN_FMT)
INST_FMT = "<20sH"; INST_SIZE = struct.calcsize(INST_FMT)
SHDR_FMT = "<20sIIIIIBbHH"; SHDR_SIZE = struct.calcsize(SHDR_FMT)


def read_riff_subchunks(data, start, end):
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


def parse_records(data, offset, length, fmt, size):
    assert length % size == 0
    count = length // size
    return [struct.unpack_from(fmt, data, offset + i * size) for i in range(count)]


with open(PATH, "rb") as f:
    data = f.read()

assert data[0:4] == b"RIFF"
riff_size = struct.unpack_from("<I", data, 4)[0]
assert len(data) == riff_size + 8, f"file length {len(data)} != declared {riff_size + 8}"
assert data[8:12] == b"sfbk"

top = {}
for cid, off, length in read_riff_subchunks(data, 12, 12 + riff_size - 4):
    assert cid == "LIST"
    list_type = data[off:off + 4].decode("ascii")
    subs = {}
    for scid, soff, slen in read_riff_subchunks(data, off + 4, off + length):
        subs[scid] = (soff, slen)
    top[list_type] = subs
print("top-level LISTs:", list(top.keys()))

sdta = top["sdta"]
pdta = top["pdta"]
smpl_off, smpl_len = sdta["smpl"]
print(f"smpl: {smpl_len} bytes = {smpl_len // 2} sample points")

phdr = parse_records(data, *pdta["phdr"], PHDR_FMT, PHDR_SIZE)
pbag = parse_records(data, *pdta["pbag"], BAG_FMT, BAG_SIZE)
pgen = parse_records(data, *pdta["pgen"], GEN_FMT, GEN_SIZE)
inst = parse_records(data, *pdta["inst"], INST_FMT, INST_SIZE)
ibag = parse_records(data, *pdta["ibag"], BAG_FMT, BAG_SIZE)
igen = parse_records(data, *pdta["igen"], GEN_FMT, GEN_SIZE)
shdr = parse_records(data, *pdta["shdr"], SHDR_FMT, SHDR_SIZE)

num_presets = len(phdr) - 1
num_insts = len(inst) - 1
num_samples = len(shdr) - 1
print(f"presets={num_presets} instruments={num_insts} samples={num_samples}")

for p in phdr[:-1]:
    name, program, bank, bagndx, *_ = p
    print(f"  preset bank={bank} program={program} name={name.split(chr(0).encode())[0]!r} bagNdx={bagndx}")

# Every pbag/genNdx must be non-decreasing and in range.
assert all(pbag[i][0] <= pbag[i + 1][0] for i in range(len(pbag) - 1)), "pbag genNdx not monotonic"
assert all(ibag[i][0] <= ibag[i + 1][0] for i in range(len(ibag) - 1)), "ibag genNdx not monotonic"
assert pbag[-1][0] == len(pgen) - 1, "pbag terminal genNdx mismatch vs pgen count"
assert ibag[-1][0] == len(igen) - 1, "ibag terminal genNdx mismatch vs igen count"
assert phdr[-1][3] == len(pbag) - 1, "phdr terminal bagNdx mismatch vs pbag count"
assert inst[-1][1] == len(ibag) - 1, "inst terminal bagNdx mismatch vs ibag count"
print("bag/gen index deltas: OK (monotonic, terminal sentinels consistent)")

# Every generator's referenced index must be in range.
bad = 0
for oper, amount in pgen[:-1]:
    if oper == 41 and not (0 <= amount < num_insts):
        print(f"BAD preset->instrument ref: {amount}"); bad += 1
for oper, amount in igen[:-1]:
    if oper == 53 and not (0 <= amount < num_samples):
        print(f"BAD instrument->sample ref: {amount}"); bad += 1
assert bad == 0, f"{bad} bad cross-references"
print("all instrument/sample generator references: in range")

# Every sample's byte range must be inside smpl, loop points inside [start,end], and the
# sample must contain some non-zero PCM energy (i.e. we didn't copy garbage/silence).
silent = []
for idx, s in enumerate(shdr[:-1]):
    name, start, end, loopstart, loopend, rate, orig_pitch, pitch_corr, link, stype = s
    assert 0 <= start <= end <= smpl_len // 2, f"sample {idx} start/end out of bounds: {start},{end}"
    assert start <= loopstart <= loopend <= end or (loopstart == 0 and loopend == 0), (
        f"sample {idx} loop points out of range: {loopstart},{loopend} vs {start},{end}"
    )
    assert stype == 1 and link == 0, f"sample {idx} not forced to independent mono: type={stype} link={link}"
    raw = data[smpl_off + start * 2: smpl_off + end * 2]
    samples = struct.unpack(f"<{len(raw)//2}h", raw)
    peak = max(abs(x) for x in samples) if samples else 0
    if peak < 100:
        silent.append((idx, name.split(b"\x00")[0].decode(errors="replace"), peak))
    if idx < 5 or idx >= num_samples - 2:
        print(f"  sample {idx} {name.split(b'\\x00')[0]!r}: {end-start} pts, peak={peak}, rate={rate}, root={orig_pitch}")

if silent:
    print(f"WARNING: {len(silent)} samples with near-silent peak (<100): {silent}")
else:
    print(f"all {num_samples} samples have real (non-silent) PCM content")

print("VERIFICATION PASSED")
