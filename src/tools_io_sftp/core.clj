(ns tools.io.sftp
  (:require [tools.io.core :refer [register-file-pred!
                                   mk-input-stream
                                   list-files]]
            [clojure.string :as str]))

;;;
;;; Implementation Predicate
;;;

(defn- sftp-file?
  [path]
  (str/starts-with? (str/lower-case (str path)) "sftp://"))

(register-file-pred! :sftp sftp-file?)

;;;
;;; Client / SSH Related
;;;


;;;
;;; Method Implementations
;;;

(defmethod list-files :sftp
  [url & [options]])
