#!/usr/bin/env python3
"""Update benchmarks/docs/results.md with the latest JMH output."""

from __future__ import annotations

import argparse
import datetime as dt
import pathlib
import re
from collections import defaultdict
from typing import List, Tuple

LINE_RE = re.compile(
    r"^(?P<benchmark>\S+)\s+"  # fully qualified benchmark name
    r"(?P<dataset>\S+)\s+"      # dataset path
    r"\S+\s+"                   # mode
    r"(?:(?P<count>\d+)\s+)?"   # optional iteration count
    r"(?P<score>[\d.]+)"         # throughput
    r"(?:\s+±\s+(?P<error>[\d.]+))?\s+ops/s$"
)

OPERATION_ORDER = {"decode": 0, "encode": 1}


class ResultEntry:
    __slots__ = ("codec", "operation", "dataset", "score", "error")

    def __init__(self, codec: str, operation: str, dataset: str, score: float, error: float | None):
        self.codec = codec
        self.operation = operation
        self.dataset = dataset
        self.score = score
        self.error = error

    def summary_key(self) -> Tuple[str, str]:
        return (self.dataset, self.operation)


def parse_results(path: pathlib.Path) -> List[ResultEntry]:
    entries: List[ResultEntry] = []
    for raw_line in path.read_text().splitlines():
        line = raw_line.strip()
        if not line or line.startswith("Benchmark"):
            continue
        match = LINE_RE.match(line)
        if not match:
            continue
        benchmark = match.group("benchmark")
        dataset = match.group("dataset")
        score = float(match.group("score"))
        error = match.group("error")
        entries.append(ResultEntry(
            codec=extract_codec(benchmark),
            operation=extract_operation(benchmark),
            dataset=dataset,
            score=score,
            error=float(error) if error else None,
        ))
    if not entries:
        raise ValueError(f"No benchmark rows found in {path}")
    return entries


def extract_codec(benchmark_name: str) -> str:
    class_name = benchmark_name.split(".")[-2]
    codec = class_name
    for suffix in ("OrderBookBenchmark", "Benchmark"):
        if codec.endswith(suffix):
            codec = codec[: -len(suffix)]
    overrides = {
        "sbe": "SBE",
        "myra": "Myra",
        "kryo": "Kryo",
        "avro": "Avro",
        "flatbuffers": "FlatBuffers",
    }
    return overrides.get(codec.lower(), codec) or class_name


def extract_operation(benchmark_name: str) -> str:
    method = benchmark_name.split(".")[-1]
    lowered = method.lower()
    if lowered.startswith("encode"):
        return "Encode"
    if lowered.startswith("decode"):
        return "Decode"
    return method


def render_number(value: float) -> str:
    return f"{value:,.3f}".rstrip("0").rstrip(".")


def render_error(value: float | None) -> str:
    return f"±{render_number(value)}" if value is not None else "—"


def render_run_section(
    timestamp: str,
    command: str,
    host: str,
    jvm: str,
    warmups: int,
    measurements: int,
    forks: int,
    warmup_time: str,
    measurement_time: str,
    entries: List[ResultEntry],
    notes: str | None,
    historical: bool = False,
) -> str:
    heading_suffix = " (historical)" if historical else ""
    lines = [
        f"### {timestamp} — `{command}`{heading_suffix}",
        "",
        f"- Host: `{host}`",
        f"- JVM: {jvm}",
        f"- Warmups: {warmups} × {warmup_time}, Measurements: {measurements} × {measurement_time}, Forks: {forks}",
    ]
    if notes:
        lines.append(f"- Notes: {notes}")
    lines.extend([
        "",
        "| Codec | Operation | Dataset | Throughput (ops/s) | Error (99.9%) |",
        "| --- | --- | --- | --- | --- |",
    ])
    for entry in sorted(entries, key=lambda e: (e.dataset, OPERATION_ORDER.get(e.operation.lower(), 99), e.codec)):
        lines.append(
            f"| {entry.codec} | {entry.operation} | {entry.dataset} | {render_number(entry.score)} | {render_error(entry.error)} |"
        )
    return "\n".join(lines)


def build_summary(entries: List[ResultEntry]) -> str:
    grouped: dict[Tuple[str, str], List[ResultEntry]] = defaultdict(list)
    for entry in entries:
        grouped[entry.summary_key()].append(entry)

    summary_lines = ["| Dataset | Operation | Codec | Throughput (ops/s) | Δ vs best (%) | Δ vs Myra (%) |",
                     "| --- | --- | --- | --- | --- | --- |"]

    for dataset, operation in sorted(grouped.keys(), key=lambda x: (x[0], OPERATION_ORDER.get(x[1].lower(), 99))):
        bucket = grouped[(dataset, operation)]
        best = max(bucket, key=lambda e: e.score).score
        myra = next((e.score for e in bucket if e.codec.lower() == "myra"), None)
        for entry in sorted(bucket, key=lambda e: e.codec):
            delta_best = percent_delta(entry.score, best)
            delta_myra = percent_delta(entry.score, myra) if myra else None
            summary_lines.append(
                "| {dataset} | {operation} | {codec} | {score} | {delta_best} | {delta_myra} |".format(
                    dataset=dataset,
                    operation=operation,
                    codec=entry.codec,
                    score=render_number(entry.score),
                    delta_best=format_percent(delta_best),
                    delta_myra=format_percent(delta_myra),
                )
            )
    return "\n".join(summary_lines)


def percent_delta(value: float, baseline: float | None) -> float | None:
    if baseline in (None, 0):
        return None
    return ((value / baseline) - 1.0) * 100.0


def format_percent(value: float | None) -> str:
    if value is None:
        return "N/A"
    return ("+" if value >= 0 else "") + f"{value:.1f}%"


def replace_block(content: str, marker: str, replacement: str) -> str:
    pattern = re.compile(rf"(<!-- {marker}_START -->)(.*?)(<!-- {marker}_END -->)", re.DOTALL)
    match = pattern.search(content)
    if not match:
        raise ValueError(f"Markers for {marker} not found in results.md")
    before, after = match.group(1), match.group(3)
    return pattern.sub(f"{before}\n{replacement}\n{after}", content, count=1)


def extract_block_body(content: str, marker: str) -> str:
    pattern = re.compile(rf"<!-- {marker}_START -->(.*?)<!-- {marker}_END -->", re.DOTALL)
    match = pattern.search(content)
    if not match:
        raise ValueError(f"Markers for {marker} not found in results.md")
    return match.group(1).strip()


def main() -> None:
    parser = argparse.ArgumentParser(description="Update benchmarks/docs/results.md with the latest JMH output.")
    parser.add_argument("--results-file", default="benchmarks/build/results/jmh/results.txt", type=pathlib.Path)
    parser.add_argument("--doc-file", default="benchmarks/docs/results.md", type=pathlib.Path)
    parser.add_argument("--command", required=True)
    parser.add_argument("--host", default="unknown-host")
    parser.add_argument("--jvm", default="unknown JVM")
    parser.add_argument("--warmups", type=int, default=5)
    parser.add_argument("--warmup-time", default="10s")
    parser.add_argument("--measurements", type=int, default=5)
    parser.add_argument("--measurement-time", default="10s")
    parser.add_argument("--forks", type=int, default=5)
    parser.add_argument("--notes")
    parser.add_argument("--timestamp")
    args = parser.parse_args()

    timestamp = args.timestamp or dt.datetime.now(dt.timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")
    entries = parse_results(args.results_file)

    latest_section = render_run_section(
        timestamp=timestamp,
        command=args.command,
        host=args.host,
        jvm=args.jvm,
        warmups=args.warmups,
        measurements=args.measurements,
        forks=args.forks,
        warmup_time=args.warmup_time,
        measurement_time=args.measurement_time,
        entries=entries,
        notes=args.notes,
    )
    history_section = render_run_section(
        timestamp=timestamp,
        command=args.command,
        host=args.host,
        jvm=args.jvm,
        warmups=args.warmups,
        measurements=args.measurements,
        forks=args.forks,
        warmup_time=args.warmup_time,
        measurement_time=args.measurement_time,
        entries=entries,
        notes=args.notes,
        historical=True,
    )

    doc_text = args.doc_file.read_text()

    # Update latest run block.
    doc_text = replace_block(doc_text, "LATEST_RUN", latest_section)

    # Prepend history entry.
    existing_history = extract_block_body(doc_text, "RUN_HISTORY")
    combined_history = history_section if not existing_history else f"{history_section}\n\n{existing_history}"
    doc_text = replace_block(doc_text, "RUN_HISTORY", combined_history)

    summary_table = build_summary(entries)
    doc_text = replace_block(doc_text, "SUMMARY_TABLE", summary_table)

    args.doc_file.write_text(doc_text)
    print(f"Updated {args.doc_file} with {len(entries)} benchmark rows from {args.results_file}.")


if __name__ == "__main__":
    main()
