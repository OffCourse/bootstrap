(ns app.core
  (:require [shared.protocols.loggable :as log]
            [cljs.nodejs :as node]))

(node/enable-util-print!)

(defn ^:export handler [event]
 (.log js/console event))

(defn -main [] identity)
(set! *main-cli-fn* -main)
