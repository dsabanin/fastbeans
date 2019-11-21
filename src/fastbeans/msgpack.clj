(ns fastbeans.msgpack
  (:require [msgpack.core :as mp])
  (:import  java.io.ByteArrayOutputStream
            java.io.DataOutput))

(defn pack
  "Pack the objects into a byte array and return it."
  [obj]
  (mp/pack obj))

(defn unpack
  [from]
  (mp/unpack from))

(extend-protocol mp/Packable
  clojure.lang.Keyword
  (packable-pack
    [kw ^DataOutput s opts]
    (mp/packable-pack (name kw) s opts))

  java.io.ByteArrayOutputStream
  (packable-pack
    [baos ^DataOutput s opts]
    (mp/packable-pack (.toString baos) s opts))

  java.util.Date
  (packable-pack
    [date ^DataOutput s opts]
    (mp/packable-pack (.intValue (/ (.getTime date) 1000)) s opts)))
