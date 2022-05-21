(ns user
  (:require [clojure.java.io :as io]
            [tools.io :as tio]
            [tools.io.sftp]
            [tools.io.sftp :as sftp]
            [clojure.string :as str])
  (:use  [clojure.reflect]
         [clojure.pprint])
  (:import [com.jcraft.jsch JSch SftpException ChannelSftp
            UserInfo]
           [java.io OutputStream InputStream]))

;;; =======================================================

(comment
  ;; -->
  (tio/list-dirs "sftp://demo:password@test.rebex.net:22/pub")
  (sftp/extract-uri "sftp://demo:password@test.rebex.net:22/pub")

  (tio/read-text-file "sftp://demo:password@test.rebex.net:22/readme.txt")
  (sftp/extract-uri "sftp://demo:password@test.rebex.net:22/readme.txt")

  ;;; Pubkey

  (let [builder (doto (JSch.)
                  (.addIdentity "dev-resources/fixture_rsa"))]
    (doto (.getSession builder "fixture" "localhost" 2222)
      (.setConfig "StrictHostKeyChecking" "no")
      (.setConfig  "PreferredAuthentications"
                   "publickey")
      (.setConfig "PubkeyAcceptedAlgorithms" "ssh-rsa")
      (.connect)))
  
  (tio/read-text-file "sftp://demo:•dev-resources/fixture_rsa@test.rebex.net:22/readme.txt")
  (sftp/extract-uri "sftp://demo:•dev-resources/fixture_rsa@test.rebex.net:22/readme.txt"))
