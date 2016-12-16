(ns app.core
  (:require [backend-shared.service.index :as service]
            [cljs.core.async :as async]
            [shared.protocols.actionable :as ac]
            [shared.protocols.convertible :as cv]
            [shared.protocols.queryable :as qa])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def adapters {:code-pipeline {}
               :bucket {:bucket-names {}}})

(defn ^:export handler [& args]
  (go
    (let [{:keys [event code-pipeline] :as service} (apply service/create adapters args)
          payload (cv/to-payload event)
          query   (cv/to-query event)
          encoded-artifacts (async/<! (qa/fetch service query))
          artifacts {} #_(ac/perform service [:decode encoded-artifacts])
          res1      {} #_(ac/perform service [:put artifacts])
          res2      {} #_(async/<! (ac/perform service [:put payload]))]
      (service/done service encoded-artifacts))))

(defn -main [] identity)
(set! *main-cli-fn* -main)
