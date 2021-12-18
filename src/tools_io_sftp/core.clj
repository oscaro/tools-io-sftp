(ns tools.io.sftp
  (:require [tools.io.core :as tio]
            [clojure.string :as str]
            [clojure.spec.alpha :as s])
  (:import [com.jcraft.jsch JSch SftpException ChannelSftp]))

;;; specs

(s/def ::username (s/and string? (comp not empty?)))
(s/def ::password (s/and string? (comp not empty?)))
(s/def ::port (partial s/int-in-range? 1 65535))
(s/def ::hostname (s/and string? (comp not empty?)))
(s/def ::resource (s/and string? (comp not empty?)))

(s/def ::sftp-resource
  (s/keys :req-un [::hostname
                   ::port
                   ::username
                   ::password
                   ::resource]))

;;; predicates

(defn- sftp-file?
  [path]
  (str/starts-with? (str/lower-case (str path)) "sftp://"))

(tio/register-file-pred! :sftp sftp-file?)

;;; Connection Helpers

(def sftp-uri
  #"^((sftp:\/\/)(\w*):(\w*)@(\w.*):(\d.*)(\/\w.*))")

(defn extract-uri
  "Convert url to spec checked map"
  [target]
  (when (re-matches sftp-uri target)
    (let [[u p h po r] (drop 3 (re-find sftp-uri target))
          res(-> {:username u :password p
                  :hostname h :port po :resource r}
                 (update :port read-string))]
      (if (s/valid? ::sftp-resource res)
        res
        (throw (Exception.
                (s/explain-str ::sftp-resource res)))))))

(defmacro with-ssh-connection
  "Wrap ssh connection context inside the body
   then gently close connection after evaluation"
  [[bname {:keys [username hostname password port] :as spec}]
   & body]
  `(let [session# (doto (.getSession (JSch.) ~username ~hostname ~port)
                    (.setConfig "StrictHostKeyChecking" "no")
                    (.setConfig "Compression"           "no")
                    (.setConfig "ControlMaster"         "no")
                    (.setPassword ~password)
                    (.connect))
         ~bname (doto (.openChannel session# "sftp")
                  (.connect))]
     (try
       ~@body
       (do
         (.disconnect ~bname)
         (.disconnect session#)))))

;;; Implementations

(defmethod tio/list-files :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      ;; Do something with
      )))

(defmethod tio/list-dirs :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      ;; Do something with
      )))

(defmethod tio/mk-input-stream :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      ;; Do something with
      )))

(defmethod tio/mk-output-stream :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      ;; Do something with
      )))
