# README for Coding Agents

This file provides guidance to coding agents (e.g., Claude Code) when working with code in this repository.

See `README.md` for API specification, usage examples, and build commands.

## Architecture

See `ARCHITECTURE.md` for the component diagram.

Key extension points registered in `plugin.xml`:
- **TermbackRestService**: HTTP endpoint (`/api/termback`) receiving notification requests from terminals
- **TermbackLocalTerminalCustomizer**: Injects session ID environment variable into terminal processes
- **TermbackTerminalToolWindowManagerListener**: Handles terminal tool window state changes to expire suppressed notifications
- **TermbackApplicationActivationListener**: Handles IDE activation to expire suppressed notifications

Data flow:
1. Terminal process sends POST to `/api/termback` with session ID and notification content
2. `TermbackRestService` → `TermbackNotifier` → `TermbackSessionRegistry` to find the session
3. Notification is displayed; clicking it activates the corresponding terminal tab

Service scopes:
- **Application-level**: `TermbackSessionRegistry`, `TermbackNotifier`

## Development

Debugging is primarily done via `./gradlew runIde`, which launches a sandbox IDE instance. 
Look for plugin logs in `build/idea-sandbox/{IDE-version}/log/idea.log`, not the system IDE's log directory.
