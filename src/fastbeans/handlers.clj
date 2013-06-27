(ns fastbeans.handlers
  (:require
   [fastbeans.msgpack :as serialization]
   [fastbeans.rpc :as rpc])
  (:use
   fastbeans.utils
   simplelog.use)
  (:import
   [org.jboss.netty.channel Channels Channel SimpleChannelHandler ExceptionEvent
    MessageEvent ChannelStateEvent]
   [org.jboss.netty.buffer ChannelBuffers ChannelBuffer]
   [java.nio.channels ClosedChannelException])
  (:gen-class
   :name "fastbeans.handlers.NettyHandler"
   :extends org.jboss.netty.channel.SimpleChannelHandler))

(defn decode-frame
  "Returns only the decoded frame payload, stripping off its length prefix"
  [frame]
  (serialization/unpack frame))

(defn encode-frame
  "Encodes a msg into a buffer-seq suitable for gloss framing"
  [msg]
  (serialization/pack msg))

(def message-buffers (atom {}))

(defn start-message
  [channel cb]
  (let [message-size (.readInt cb)
        ba (byte-array message-size)
        read-size (.readableBytes cb)]
    (debug "message-size:" message-size "read-size" read-size)
    (.readBytes cb ba 0 read-size)
    (swap! message-buffers assoc channel [read-size ba])))

(defn continue-message
  [channel [accumulated-size ba] cb]
  (let [message-size (alength ba)
        read-size (.readableBytes cb)]
    (debug "message-size:" message-size "accumulated-size" accumulated-size "read-size" read-size)
    (.readBytes cb ba accumulated-size read-size)
    (swap! message-buffers assoc channel [(+ accumulated-size read-size) ba])))

(defn message-finished?
  [[read-size ba]]
  (= read-size (alength ba)))

(defn finish-message
  [c [_ frame]]
  (swap! message-buffers dissoc c)
  (let [msg (decode-frame frame)
        reply ^"[B" (encode-frame (rpc/dispatch msg))]
    (.write c ^ChannelBuffer (ChannelBuffers/wrappedBuffer reply))))

(defn write-to-channel
  [^Channel c ^"[B" data]
  (.write c ^ChannelBuffer (ChannelBuffers/wrappedBuffer data)))

(defn -messageReceived [this ctx ^MessageEvent e]
  (let [c (.getChannel e)
        cb ^ChannelBuffer (.getMessage e)]
    (if-let [message (@message-buffers c)]
      (continue-message c message cb)
      (start-message c cb))
    (let [message (@message-buffers c)]
      (when (message-finished? message)
        (finish-message c message)))))

(defn write-death-frame
  [channel throwable]
  (let [death-frame (rpc/die :failed-with-network-exception (str throwable))]
    (write-to-channel channel (encode-frame death-frame))
    (.close channel)))

(defn -exceptionCaught
  [this ctx ^ExceptionEvent e]
  (let [throwable (.getCause e)
        channel (.getChannel e)]
    (error "Exception caught:" throwable)
    (print-exception throwable)
    (cond
     (= (.getClass throwable) java.io.IOException) (.close channel)
     (not= (.getClass throwable) ClosedChannelException) (write-death-frame channel throwable))))

(defn -channelClosed
  [this ctx ^ChannelStateEvent e]
  (swap! message-buffers dissoc (.getChannel e)))
