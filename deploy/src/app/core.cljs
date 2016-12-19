(ns app.core
  (:require [backend-shared.service.index :as service]
            [cljs.core.async :as async]
            [shared.protocols.actionable :as ac]
            [shared.protocols.convertible :as cv]
            [shared.protocols.queryable :as qa]
            [shared.protocols.loggable :as log])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def adapters {:code-pipeline {}
               :bucket {:bucket-names {}}})

(defn ^:export handler [& args]
  (go
    (let [{:keys [event] :as service} (apply service/create adapters args)
          payload                     (cv/to-payload event)
          query                       (cv/to-query event)
          {:keys [found error]}       (async/<! (qa/fetch service query))
          artifacts                   (when found (async/<! (ac/perform service [:decode found])))
          res1      {} #_(ac/perform service [:put artifacts])
          res2      {} #_(async/<! (ac/perform service [:put payload]))]
      (log/log "RESTART PIPELINE")
      (service/done service artifacts))))

(defn -main [] identity)
(set! *main-cli-fn* -main)
