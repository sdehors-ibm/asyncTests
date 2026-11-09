#!/usr/bin/env python3
"""Tiny Prometheus exporter: total memory (RSS / virtual) of the Liberty process.

Liberty's /metrics only exposes JVM heap data; the OS-level process memory is
read here with `ps` and served in Prometheus text format. Stdlib only.

    python3 collector/process_exporter.py            # serves 127.0.0.1:9101/metrics
"""
import argparse
import subprocess
from http.server import BaseHTTPRequestHandler, HTTPServer


def find_pid(pattern):
    try:
        res = subprocess.run(["pgrep", "-f", pattern], capture_output=True, text=True, timeout=5)
    except (OSError, subprocess.SubprocessError):
        return None
    pids = res.stdout.split()
    return int(pids[0]) if pids else None


def read_memory(pid):
    """Return (rss_bytes, vsz_bytes) or None. `ps` reports KiB."""
    try:
        res = subprocess.run(["ps", "-o", "rss=,vsz=", "-p", str(pid)], capture_output=True, text=True, timeout=5)
        rss, vsz = res.stdout.split()
        return int(rss) * 1024, int(vsz) * 1024
    except (OSError, ValueError, subprocess.SubprocessError):
        return None


def render(pattern):
    pid = find_pid(pattern)
    mem = read_memory(pid) if pid else None
    lines = [
        "# HELP liberty_process_up 1 if the Liberty process was found.",
        "# TYPE liberty_process_up gauge",
        f"liberty_process_up {1 if mem else 0}",
    ]
    if mem:
        lines += [
            "# HELP liberty_process_resident_memory_bytes Resident set size of the Liberty process.",
            "# TYPE liberty_process_resident_memory_bytes gauge",
            f"liberty_process_resident_memory_bytes {mem[0]}",
            "# HELP liberty_process_virtual_memory_bytes Virtual memory size of the Liberty process.",
            "# TYPE liberty_process_virtual_memory_bytes gauge",
            f"liberty_process_virtual_memory_bytes {mem[1]}",
        ]
    return ("\n".join(lines) + "\n").encode()


def make_handler(pattern):
    class Handler(BaseHTTPRequestHandler):
        def do_GET(self):
            if self.path != "/metrics":
                self.send_error(404)
                return
            body = render(pattern)
            self.send_response(200)
            self.send_header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

        def log_message(self, *_):
            pass

    return Handler


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--host", default="127.0.0.1", help="bind address (default: %(default)s)")
    ap.add_argument("--port", type=int, default=9101, help="bind port (default: %(default)s)")
    ap.add_argument("--match", default="ws-server.jar", help="pgrep -f pattern for the process (default: %(default)s)")
    args = ap.parse_args()
    print(f"Serving http://{args.host}:{args.port}/metrics (process match: {args.match})", flush=True)
    HTTPServer((args.host, args.port), make_handler(args.match)).serve_forever()


if __name__ == "__main__":
    main()
