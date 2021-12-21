(ns user
  (:require [clojure.java.io :as io]
            [tools.io :as tio]
            [tools.io.sftp]
            [clojure.string :as str])
  (:use  [clojure.reflect]
         [clojure.pprint])
  (:import [com.jcraft.jsch JSch SftpException ChannelSftp]
           [java.io OutputStream InputStream]))

;;; =======================================================

(comment
  (tio/list-dirs "sftp://demo:password@test.rebex.net:22/")
  )
