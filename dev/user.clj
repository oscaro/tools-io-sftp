(ns user
  (:require [clojure.java.io :as io]
            [tools.io :as tio])
  (:import [com.jcraft.jsch JSch SftpException ChannelSftp]))

;;; Macro context dev

(defmacro with-ssh-connection
  [binding & body]
  `(let [server-spec# ~(second binding)
         ;; Deconstruct ?
         session# (doto (.getSession (JSch.) (:user server-spec#) (:host server-spec#) (:port server-spec#))
                    (.setConfig "StrictHostKeyChecking" "no")
                    (.setConfig "Compression"           "no")
                    (.setConfig "ControlMaster"         "no")
                    (.setPassword (:password server-spec#))
                    (.connect))
         ~(first binding) (doto (.openChannel session# "sftp")
                            (.connect))]
     (try
       ~@body
       (finally
         (.disconnect ~(first binding))
         (.disconnect session#)))))
