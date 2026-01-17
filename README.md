# termback

<!-- Plugin description -->
Bridges between IntelliJ IDEA's integrated terminal and its notification system. Terminal processes can send notifications via a local HTTP endpoint, and clicking one focuses the tab that sent it.
<!-- Plugin description end -->

## Features

- Exposes a local HTTP endpoint for terminal processes to send IDE notifications
- Focuses the terminal tab on notification click
- Automatically suppresses notifications when the tab is already active or visible
- Provides a keyboard shortcut to pick a notification and jump to its terminal tab

## Requirements

- IntelliJ IDEA 2025.3.1 or later (or compatible IDE)

## Installation

1. Download ZIP from [Releases](https://github.com/hirofumi/termback/releases)
2. Settings → Plugins → ⚙️ → Install Plugin from Disk...

## Usage

When you open a terminal tab in IntelliJ, the plugin sets:

| Variable | Description |
|----------|-------------|
| `TERMBACK_ENDPOINT` | Notification endpoint URL |
| `TERMBACK_SESSION_ID` | UUID identifying the tab |

Send a notification:

```bash
curl -fsS --json "$(jq -n --arg msg "Done" '{sessionId: env.TERMBACK_SESSION_ID, message: $msg}')" "$TERMBACK_ENDPOINT"
```

Press `Alt+Shift+T` (default) to pick a notification and jump to its terminal tab.

### Examples

#### Shell Function

```bash
termback() {
  test -z "$TERMBACK_ENDPOINT" && return 0
  curl -fsS --json "$(jq -n --arg msg "$1" '{sessionId: env.TERMBACK_SESSION_ID, message: $msg}')" "$TERMBACK_ENDPOINT" > /dev/null 2>&1
}

# Example
./build.sh && termback "Build completed"
```

#### Claude Code Hooks

Add to `.claude/settings.json` to receive IDE notifications when Claude Code is waiting for input:

```json
{
  "hooks": {
    "Notification": [
      {
        "matcher": "",
        "hooks": [
          {
            "type": "command",
            "command": "test -z \"$TERMBACK_ENDPOINT\" || { jq '{sessionId: env.TERMBACK_SESSION_ID, message: .message}' | curl -fsS --json @- \"$TERMBACK_ENDPOINT\" > /dev/null; }"
          }
        ]
      }
    ]
  }
}
```

## API

### Endpoint

`POST /api/termback`

### Request

| Field | Type | Required | Default | Description |
|-------|------|:--------:|---------|-------------|
| `sessionId` | string | ✓ | | Tab session ID |
| `message` | string | ✓ | | Notification body |
| `title` | string | | `null` | Notification title |
| `broadcast` | boolean | | `true` | true: all projects, false: session's project only |
| `suppress` | string | | `"whenActive"` | Suppress condition |
| `onNext` | string | | `"expire"` | Behavior on next notification |

#### suppress

Controls notification suppression based on tab state. When the condition is met:
- At creation time: notification is skipped (not created)
- After creation: notification is expired (dismissed)

| Value | Behavior |
|-------|----------|
| `"none"` | Never suppress |
| `"whenActive"` | Suppress when tab is active (selected and tool window has focus) |
| `"whenVisible"` | Suppress when tab is visible (selected and tool window is open) |

#### onNext

Controls behavior when a new notification arrives for the same tab.

| Value | Behavior |
|-------|----------|
| `"keep"` | Keep notification when a new one arrives |
| `"expire"` | Expire notification when a new one arrives |

### Response

| Status | Description |
|--------|-------------|
| 202 | Request accepted for processing |
| 400 | Invalid request |
| 404 | Session not found |

A 202 response indicates the request was accepted. The notification may be silently skipped if `suppress` conditions are met.

## Development

### Requirements

- JDK 21

### Commands

```bash
./gradlew build        # Build
./gradlew buildPlugin  # Build plugin ZIP (build/distributions/)
./gradlew runIde       # Run in sandbox IDE (logs: build/idea-sandbox/{IDE-version}/log/idea.log)
./gradlew test         # Test
```
