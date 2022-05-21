(ns tools.io.sftp
  (:require [tools.io :as utils]
            [tools.io.core :as tio]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log])
  (:import [com.jcraft.jsch JSch SftpException]))

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

(defn is-password-pubkey?
  "Since dispatch is made only in
   the url we need to store that information inside.

  Assuming password don't contains special
  characters, we'll use an specific char to flag
  pubkey usage.

  Note: THIS IS A VERY BAD IMPLEMENTATION, but probably
    the only hacky way to do this (since I need to have
    this feature realy quickly I don't spent to much time
    to find a clever approach tbh).

  Note 2: Recent versions of OpenSSH (7.8 and newer) generate
     keys in new OpenSSH format by default, which starts with:

     `-----BEGIN OPENSSH PRIVATE KEY-----`

     JSch does not support this key format.
     You can use ssh-keygen to convert the key to the classic OpenSSH format:

     $ ssh-keygen -p -f file -m pem -P passphrase -N passphrase

     If you are creating a new key with ssh-keygen, just add -m PEM to
     generate the new key in the classic format:

     $ ssh-keygen -m PEM

  Using bullet char as flag:
    - `UTF-8`: 0xE2 0x80 0xA2
    - `UTF-16`: 0x2022"
  [pwd]
  (str/starts-with? pwd "•"))

(def is-password (comp not is-password-pubkey?))

(defn extract-uri
  "Convert url to spec checked map"
  [target]
  (let [url (str/replace target #"sftp://" "")
        [u & [p1 p2]] (str/split url #":" 3)
        [h p] (-> (str/reverse p1)
                  (str/split #"@" 2)
                  (->> (map str/reverse)))
        [po r] (str/split p2 #"/" 2)
        res (-> {:username u :password p
                 :hostname h :port po :resource (str "/" r)}
                (update :port (fn [e] (when e (Integer/parseInt e)))))]
    (if (s/valid? ::sftp-resource res)
      res
      (throw (Exception.
              (s/explain-str ::sftp-resource res))))))

(defmacro with-ssh-connection
  "Wrap ssh connection context inside the body
   then gently close connection after evaluation"
  [[bname spec]  & body]
  `(let [server-spec# ~spec
         session-builder# (if (is-password-pubkey? (:password server-spec#))
                            (doto (JSch.)
                              (.addIdentity (apply str (rest (:password server-spec#)))))
                            (JSch.))
         session# (doto (.getSession session-builder#
                                     (:username server-spec#)
                                     (:hostname server-spec#)
                                     (:port server-spec#))
                    (cond-> (is-password (:password server-spec#))
                      (.setPassword (:password server-spec#)))
                    (.setConfig "StrictHostKeyChecking" "no")
                    (.setConfig "Compression"           "no")
                    (.setConfig "ControlMaster"         "no")
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
             (map (fn [e] (if (= "/" (:resource conn-spec))
                           (str "/" (.getFilename e))
                           (str (:resource conn-spec)
                                "/" (.getFilename e))))))
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
      (utils/with-tempfile [tmp-file]
        (.get conn (:resource conn-spec) tmp-file)
        {:stream (io/input-stream tmp-file)}))))

(defmethod tio/mk-output-stream :sftp
  [url & [options]]
  (if-let [conn-spec (extract-uri url)]
    (with-ssh-connection [conn conn-spec]
      (utils/with-tempfile [tmp-file]
        (let [stream (io/output-stream tmp-file)]
          (try
            {:stream stream}
            (finally
              (.put conn tmp-file
                    (:resource conn-spec)))))))))
