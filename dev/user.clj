(ns user
  (:require [clojure.java.io :as io]
            [tools.io :as tio]
            [tools.io.sftp :refer [with-ssh-connection]]
            [clojure.string :as str])
  (:use  [clojure.reflect]
         [clojure.pprint])
  (:import [com.jcraft.jsch JSch SftpException ChannelSftp]))

;;; Macro context dev


(defn hello-ssh-context []
  (with-ssh-connection [foo {:password "password"
                             :username "demo"
                             :hostname "test.rebex.net"
                             :port 22}]
    ;;    (pprint (reflect foo))
    (let [path "/"
          r (->> (.ls foo path)
                 (map (fn [e] (.getLongname e)))
                 (filter #(= \d (first %))))]
      (->> (map #(str/split % #" ") r)
           (map last)
           (remove #(contains? #{"." ".."} %)))
      
      )))
