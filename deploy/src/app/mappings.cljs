(ns app.mappings
  (:require [backend-shared.service.index :refer [perform fetch]]
            [shared.protocols.actionable :as ac])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defmethod fetch :artifacts [_ query]
  (go query))

(defmethod perform [:put :pipeline-job] [{:keys [code-pipeline]} action]
  (ac/perform code-pipeline action))
