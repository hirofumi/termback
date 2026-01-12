# Termback Architecture

## Components

```
                    +------------------------------+   bindSelected()  +------------------------------+
                    | TermbackSettingsConfigurable |------------------>| TermbackSettings             |
                    +------------------------------+                   | (Application-level Service)  |
                                                                       +------------------------------+
                                                                                                ^
                                                                skipPopupWhenSingleNotification |
                                                                                                |
                 +------------------------------------------------------+                       |
                 |                                                      |                       |
                 |             +---------------------------------+  +----------------------------------+
                 |   +---------| TermbackLocalTerminalCustomizer |  | TermbackSelectNotificationAction |
                 |   |         | (Terminal Extension)            |  | (Alt + Shift + T)                |
                 |   |         +---------------------------------+  +----------------------------------+
                 |   |                |  |                                         |                |
                 |   | register()     |  |                               +---------+                |
getAllSessions() |   | unregister()   |  |                               |                          |
                 v   v                |  |                               |  +-----------------------+--+
  +-----------------------------+     |  |   getUnexpiredNotifications() |  | navigateToTab()          |
  | TermbackSessionRegistry     |     |  |                               v  v                          |
  | (Application-level Service) |     |  |           new()  +-----------------+   create()  +----------------------+
  +-----------------------------+     |  +----------------->| TermbackSession |------------>| TermbackNotification |
    ^                                 |                     +-----------------+             +----------------------+
    | findById(), findByContent()     +-----+                 ^                                 ^              |
    |                                       |                 | postNotification()              | expire()     |
    |                                       |                 | takeSuppressedNotifications()   |              |
    |          expireSessionNotifications() |                 | takeAllNotifications()          |              |
    |                                       v                 |                                 |              |
    |                                    +-----------------------------+                        |              |
    +------------------------------------| TermbackNotifier            |------------------------+              |
                                         | (Application-level Service) |                                       |
                                         +-----------------------------+                +----------------------+
                                                ^     ^                                 | notify(), expire()
                expireSuppressedNotifications() |     | notify()                        v
  +-----+---------------------------------------+     |                         +--------------+
  |     |                                             |                         | Notification |
  |     |                                             |                         +--------------+
  |  +---------------------------------------+   +---------------------+
  |  | TermbackApplicationActivationListener |   | TermbackRestService |
  |  | (Application-level Listener)          |   | (HTTP endpoint)     |
  |  +---------------------------------------+   +---------------------+
  |
+-------------------------------------------+ 
| TermbackTerminalToolWindowManagerListener |
| (Project-level Listener)                  |
+-------------------------------------------+
```
