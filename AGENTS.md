# README for Coding Agents

This file provides guidance to coding agents (e.g., Claude Code) when working with code in this repository.

See `README.md` for API specification, usage examples, and build commands.

## Architecture

See `ARCHITECTURE.md` for the component diagram.

## Development

Debugging is primarily done via `./gradlew runIde`, which launches a sandbox IDE instance.
Look for plugin logs in `build/idea-sandbox/{IDE-version}/log/idea.log`, not the system IDE's log directory.
