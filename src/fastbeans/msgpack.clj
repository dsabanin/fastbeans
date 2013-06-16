(ns fastbeans.msgpack
  (:require
    [clj-msgpack.core :as mp]
    [clojure.java.io :as io])
  (:import
    [org.msgpack MessagePack]
    [org.msgpack.packer Packer]))

(def packer (MessagePack.))

(defn pack
  "Pack the objects into a byte array and return it."
  [& objs]
  (let [p (.createBufferPacker ^MessagePack packer)]
    (apply mp/pack-into p objs)
    (.toByteArray p)))

(defn unpack
  [from]
  (let [is (io/input-stream from)
        u (.createUnpacker ^MessagePack packer is)]
    (map mp/unwrap u)))

(extend-protocol mp/Packable
  clojure.lang.Keyword
  (pack-me [kw ^Packer packer]
    (.write packer ^String (name kw)))

  java.io.ByteArrayOutputStream
  (pack-me [baos ^Packer packer]
    (.write packer ^String (.toString baos)))

  java.util.Date
  (pack-me [date ^Packer packer]
    (.write packer ^Number (/ (.getTime date) 1000))))
