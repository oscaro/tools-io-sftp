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
  ;; -->
  (tio/read-csv-file "sftp://demo:password@test.rebex.net:22/readme.txt")

  (tio/write-text-file "sftp://demo:password@test.rebex.net:22/yool.txt"
                       ["eu"])
  )
