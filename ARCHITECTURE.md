# Termback Architecture

## Components

```
+------------------------------+   bindSelected()  +------------------------------+  notificationDestination
| TermbackSettingsConfigurable |------------------>| TermbackSettings             |<--------------------------+
+------------------------------+                   | (Application-level Service)  |                           |
                                                   +------------------------------+                           |
                                                                       ^                                      |
                 +-------------------------------------------------+   | skipPopupWhenSingleNotification      |
                 |                                                 |   |                                      |
                 |       +-----------------------------+       +----------------------------------+           |
                 |   +---| TermbackPostStartupActivity |       | TermbackSelectNotificationAction |------+    |
                 |   |   | (Project Activity)          |       | (Alt + Shift + T)                |      |    |
                 |   |   +-----------------------------+       +----------------------------------+      |    |
                 |   |                     |  |                               |                          |    |
                 |   | register()          |  |                               |                          |    |
getAllSessions() |   | unregister()        |  |                               |                          |    |
                 v   v                     |  |                               |  +-----------------------+    |
  +-----------------------------+          |  |   getUnexpiredNotifications() |  | navigateToTab()       |    |
  | TermbackSessionRegistry     |          |  |                               v  v                       |    |
  | (Application-level Service) |          |  |           new()  +-----------------+                     |    |
  +-----------------------------+          |  +----------------->| TermbackSession |-----------------+   |    |
    ^                                      |                     +-----------------+                 |   |    |
    | findById(), getAllSessions()         |                       ^                                 |   |    |
    |                                      |                       | addNotification()               |   |    |
    |                                      |                       | takeSuppressedNotifications()   |   |    |
    |         expireSessionNotifications() |                       | takeAllNotifications()          |   |    |
    |                                      v                       |                                 |   |    |
    |                                   +-----------------------------+                              |   |    |
    +-----------------------------------| TermbackNotifier            |----------------+-------------+   |    |
                                        | (Application-level Service) |                |                 |    |
                                        +-----------------------------+                | expire()        |    |
                                          ^           ^             |                  v                 |    |
          expireSuppressedNotifications() |           | notify()    |   +----------------------------+   |    |
  +-----+---------------------------------+           |             |   | TermbackNotificationHandle |   |    |
  |     |                                             |             |   | (IDE / macOS)              |   |    |
  |     |                                             |             |   +----------------------------+   |    |
  |  +-------------------------------------------+    |             |           ^            |           |    |
  |  | TermbackTerminalToolWindowManagerListener |    |             |           | postTo*()  | expire()  |    |
  |  | (Project-level Listener)                  |    |             |           |            v           |    |
  |  +-------------------------------------------+    |             |           |  +-----------------------+  |
  |                                                   |             |           |  | Platform Notification |  |
+---------------------------------------+  +---------------------+  |           |  | (IDE / macOS)         |  |
| TermbackApplicationActivationListener |  | TermbackRestService |  |           |  +-----------------------+  |
| (Application-level Listener)          |  | (HTTP endpoint)     |  |           |                             |
+---------------------------------------+  +---------------------+  |           |                             |
                                                         ^          |           |                             |
+------------------------------------+  getEndpointUrl() |          | post()  +-----------------------------+ |
| TermbackShellExecOptionsCustomizer |-------------------+          +-------->| TermbackNotificationChannel |-+
| (Terminal Extension)               |                                        | (IDE / macOS)               |
+------------------------------------+                                        +-----------------------------+
```
