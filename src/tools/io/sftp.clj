(ns tools.io.sftp
  (:require [tools.io :as utils]
            [tools.io.core :as tio]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log])
  (:import [com.jcraft.jsch JSch SftpException
            ChannelSftp]))

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
  #"^((sftp:\/\/)(\w*):(\w*)@(\w.*):(\d.)(\/(\w.*)?))")

(defn extract-uri
  "Convert url to spec checked map"
  [target]
  (if (re-matches sftp-uri target)
    (let [[u p h po r & rest] (drop 3 (re-find sftp-uri target))
          res (-> {:username u :password p
                   :hostname h :port po :resource r}
                  (update :port (fn [e] (when e (Integer/parseInt e)))))]
      (if (s/valid? ::sftp-resource res)
        res
        (throw (Exception.
                (s/explain-str ::sftp-resource res)))))
    (throw (Exception. "not an sftp url"))))

(defmacro with-ssh-connection
  "Wrap ssh connection context inside the body
   then gently close connection after evaluation"
  [[bname spec]  & body]
  `(let [server-spec# ~spec
         session# (doto (.getSession (JSch.)
                                     (:username server-spec#)
                                     (:hostname server-spec#)
                                     (:port server-spec#))
                    (.setConfig "StrictHostKeyChecking" "no")
                    (.setConfig "Compression"           "no")
                    (.setConfig "ControlMaster"         "no")
                    (.setPassword (:password server-spec#))
                    (.connect))
         ~bname (doto (.openChannel session# "sftp")
                  (.connect))]
     (try
       (log/debug "sftp channel instanciated with spec" ~spec)
       ~@body
       (finally
         (.disconnect ~bname)
         (.disconnect session#)))))

;;; Implementations

(defmethod tio/list-files :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      (try
        (->> (.ls conn (:resource conn-spec))
             (remove #(contains? #{"." ".."} (.getFilename %)))
             (map (fn [e] (str (:resource conn-spec)
                               "/" (.getFilename e)))))
        (catch SftpException e
          nil)))))

(defmethod tio/list-dirs :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      (try
        (let [path (:resource conn-spec)
              r (->> (.ls conn path)
                     (map (fn [e] (.getLongname e)))
                     (filter #(= \d (first %))))]
          (->> (map #(str/split % #" ") r)
               (map last)
               (remove #(contains? #{"." ".."} %))
               (map #(str "/" %))))
        (catch Exception e
          nil)))))

(defmethod tio/mk-input-stream :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      ;; TODO
      {:stream nil})))

(defmethod tio/mk-output-stream :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      (utils/with-tempfile [tmp-file]
        (.get conn (:resource conn-spec) tmp-file)
        {:stream (io/output-stream tmp-file)}))))