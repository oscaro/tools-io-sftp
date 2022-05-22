# tools-io-sftp [![Clojars Project](https://img.shields.io/clojars/v/com.oscaro/tools-io-sftp.svg)](https://clojars.org/com.oscaro/tools-io-sftp)

SFTP Implementation for tools-io

## Usage

### Plugin

Import implementations to your namespace:

```clojure
(ns user
  (:require [tools.io :as tio]
            [tools.io.sftp]]))
```

### Using tools.io API

```clojure

;; List dirs in root
(tio/list-dirs "sftp://demo:password@test.rebex.net:22/")
;; => ("/pub" ...)

;; List subdirs
(tio/list-dirs "sftp://demo:password@test.rebex.net:22/pub")
;; => ("/pub/foobar" ...)

;; List files in dir
(tio/list-files "sftp://demo:password@test.rebex.net:22/pub")
;; => ("/pub" "readme.txt" ...)

;; Read file ...
(tio/read-text-file "sftp://demo:password@test.rebex.net:22/readme.txt")
;; => ("Welcome," "Your are connected to" ...)

```

### Testing

1. Bootstrap a local ssh daemon

```bash
$ sudo docker-compose up -d
```

2. Launch Tests

```bash
$ lein test
```

## Important SSH Notes

Since version [8.8](https://www.openssh.com/txt/release-8.8),  old ssh-rsa algorithm 
was disabled default, and other clients are using a newer protocol.

To use old keys, ensure that the daemon have this support
in his configuration:

```bash
$ cat /etc/ssh/sshd_config
PubkeyAuthentication yes
PubkeyAcceptedKeyTypes=+ssh-rsa
```

## Changelog

### 0.1.3
	- Public key suppord & password key decryption
### 0.1.2
	- First stable version

## License

Copyright © 2021 oscaro

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
