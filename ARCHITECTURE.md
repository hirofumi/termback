# Termback Architecture

## Components

```
                                              +---------------------------------+
                                              | TermbackLocalTerminalCustomizer |--+
+-------------------------------------------+ | (Terminal Extension)            |  |
| TermbackTerminalToolWindowManagerListener | +---------------------------------+  | register(), unregister()
| (Project-level Listener)                  |   |                                  v
+-------------------------------------------+   |      +-----------------------------+
  |                                             |      | TermbackSessionRegistry     |
  |  +---------------------------------------+  |      | (Application-level Service) |
  |  | TermbackApplicationActivationListener |  |      +-----------------------------+
  |  | (Application-level Listener)          |  |                                  ^
  |  +---------------------------------------+  | expireSessionNotifications()     | findById(), findByContent()
  |    |                                        v                                  |
  |    |      expireSuppressedNotifications() +-----------------------------+      |
  +----+------------------------------------->| TermbackNotifier            |------+
                                              | (Application-level Service) |
                                              +-----------------------------+
                                                ^  |                    |
             +---------------------+   notify() |  |                    | createSimple(), addAction()
             | TermbackRestService |------------+  |                    v
             | (HTTP endpoint)     |               |   expire()  +--------------+
             +---------------------+               +------------>| Notification |
                                                   |             +--------------+
                                                   |                    |
                                                   +--------------------+
```
