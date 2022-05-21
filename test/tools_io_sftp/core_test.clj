(ns tools-io-sftp.core-test
  (:require [clojure.test :refer :all]
            [tools.io.sftp :refer :all]
            [tools.io :as tio]))

(deftest extractor
  (testing "resourcename extractor"
    (let [init-1 "sftp://foo:bar@hello.world.fr:22/euoeuoe"
          rest-1 {:username "foo"
                  :password "bar" :hostname "hello.world.fr"
                  :port 22 :resource "/euoeuoe"}]
      (is (= rest-1 (extract-uri init-1)))
      (is (true? (is-password (:password rest-1))))))
  (testing "pubkey support"
    (let [init-1 "sftp://foo:•/home/iomonad/foo.pub@hello.world.fr:22/euoeuoe"
          rest-1 {:username "foo"
                  :password "•/home/iomonad/foo.pub" :hostname "hello.world.fr"
                  :port 22 :resource "/euoeuoe"}]
      (is (= rest-1 (extract-uri init-1)))
      (is (true? (is-password-pubkey? (:password rest-1)))))))

(deftest pass-auth-test
  (testing "pass auth is working"
    (let [url "sftp://fixture:h4ckm3@localhost:2222/etc/motd"
          result (tio/read-text-file url)]
      (is (= "Welcome to OpenSSH Server" (first result))))))

(deftest public-key-auth-test
  (testing "public key auth is working"
    (let [url "sftp://fixture:•dev-resources/fixture_rsa@localhost:2222/etc/motd"
          result (tio/read-text-file url)]
      (is (= "foo" result)))))
