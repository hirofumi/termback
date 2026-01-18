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
                 |       +---------------------------------+   +----------------------------------+           |
                 |   +---| TermbackLocalTerminalCustomizer |   | TermbackSelectNotificationAction |--+        |
                 |   |   | (Terminal Extension)            |   | (Alt + Shift + T)                |  |        |
                 |   |   +---------------------------------+   +----------------------------------+  |        |
                 |   |                |  |                               |                           |        |
                 |   | register()     |  |                               |                           |        |
getAllSessions() |   | unregister()   |  |                               |                           |        |
                 v   v                |  |                               |  +------------------------+        |
  +-----------------------------+     |  |   getUnexpiredNotifications() |  | navigateToTab()        |        |
  | TermbackSessionRegistry     |     |  |                               v  v                        |        |
  | (Application-level Service) |     |  |           new()  +-----------------+                      |        |
  +-----------------------------+     |  +----------------->| TermbackSession |------------------+   |        |
    ^                                 |                     +-----------------+                  |   |        |
    | findById(), findByContent()     |                       ^                                  |   |        |
    |                                 |                       | addNotification()                |   |        |
    |                                 |                       | takeSuppressedNotifications()    |   |        |
    |    expireSessionNotifications() |                       | takeAllNotifications()           |   |        |
    |                                 v                       |                                  |   |        |
    |                              +-----------------------------+                               |   |        |
    +------------------------------| TermbackNotifier            |---------------+---------------+   |        |
                                   | (Application-level Service) |               |                   |        |
                                   +-----------------------------+               | expire()          |        |
                                     ^            ^       |                      v                   |        |
     expireSuppressedNotifications() |   notify() |       |   +----------------------------+         |        |
  +-----+----------------------------+            |       |   | TermbackNotificationHandle |         |        |
  |     |                                         |       |   | (IDE / macOS)              |         |        |
  |     |                                         |       |   +----------------------------+         |        |
  |  +---------------------------------------+    |       |               ^      |                   |        |
  |  | TermbackApplicationActivationListener |    |       |     postTo*() |      | expire()          |        |
  |  | (Application-level Listener)          |    |       |               |      v                   |        |
  |  +---------------------------------------+    |       |               |    +-----------------------+      |
  |                                               |       |               |    | Platform Notification |      |
+-------------------------------------------+     |       |               |    | (IDE / macOS)         |      |
| TermbackTerminalToolWindowManagerListener |     |       |               |    +-----------------------+      |
| (Project-level Listener)                  |     |       |               |                                   |
+-------------------------------------------+     |       | post()        |                                   |
                                                  |       v               |                                   |
                       +---------------------+    |   +-----------------------------+                         |
                       | TermbackRestService |----+   | TermbackNotificationChannel |-------------------------+
                       | (HTTP endpoint)     |        | (IDE / macOS)               |
                       +---------------------+        +-----------------------------+
```
