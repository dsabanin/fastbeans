(ns fastbeans.core
  (:require [fastbeans.handlers :as handler])
  (:import [java.net InetSocketAddress]
           [java.util.concurrent Executors]
           [org.jboss.netty.bootstrap ServerBootstrap]
           [org.jboss.netty.channel AdaptiveReceiveBufferSizePredictorFactory]
           [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]))

(defn channel-factory []
  (NioServerSocketChannelFactory. (Executors/newCachedThreadPool)
                                  (Executors/newCachedThreadPool)))

(defn buffer-size-predictor [min avg max]
  (AdaptiveReceiveBufferSizePredictorFactory. min avg max))

(defn start
  "Start the RPC server. Returns Netty pipeline."
  [port]
  (let [bootstrap (ServerBootstrap. (channel-factory))
        pipeline (.getPipeline bootstrap)
        handler (fastbeans.handlers.NettyHandler.)]
    (.addLast pipeline "handler" handler)
    (.setOption bootstrap "child.tcpNoDelay", true)
    (.setOption bootstrap "child.keepAlive", true)
    (.setOption bootstrap "child.receiveBufferSizePredictorFactory"
                (buffer-size-predictor 1024 4096 8192))
    (.bind bootstrap (InetSocketAddress. port))
    pipeline))
