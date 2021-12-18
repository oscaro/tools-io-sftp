(ns tools-io-sftp.core-test
  (:require [clojure.test :refer :all]
            [tools.io.sftp :refer :all]))

(deftest extractor
  (testing "resourcename extractor"
    (let [init-1 "sftp://foo:bar@hello.world.fr:22/euoeuoe"
          rest-1 {:username "foo"
                  :password "bar" :hostname "hello.world.fr"
                  :port 22 :resource "/euoeuoe"}]
      (is (= rest-1 (extract-uri init-1))))))
