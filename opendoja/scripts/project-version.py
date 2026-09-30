#!/usr/bin/env python3
import argparse
import re
import sys
import xml.etree.ElementTree as ET


POM_NAMESPACE = {"m": "http://maven.apache.org/POM/4.0.0"}
SAFE_VERSION_CHARS = re.compile(r"[^0-9A-Za-z._+-]+")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Read a Maven project version and strip unsafe characters."
    )
    parser.add_argument("pom_path", nargs="?", default="pom.xml")
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    try:
        root = ET.parse(args.pom_path).getroot()
    except (ET.ParseError, OSError) as exc:
        print(f"failed to read {args.pom_path}: {exc}", file=sys.stderr)
        return 1

    version_node = root.find("m:version", POM_NAMESPACE)
    if version_node is None or version_node.text is None:
        print(f"missing project version in {args.pom_path}", file=sys.stderr)
        return 1

    sanitized_version = SAFE_VERSION_CHARS.sub("", version_node.text.strip())
    if not sanitized_version:
        print(f"sanitized version from {args.pom_path} is empty", file=sys.stderr)
        return 1

    print(sanitized_version)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
