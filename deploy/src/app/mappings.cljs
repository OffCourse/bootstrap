(ns app.mappings
  (:require [backend-shared.service.index :refer [perform fetch]]
            [shared.protocols.actionable :as ac]
            [cljs.core.async :as async]
            [shared.protocols.queryable :as qa])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defmethod fetch :artifacts [{:keys [bucket]} {:keys [input-queries credentials] :as query}]
  (go
    (let [{:keys [found errors]} (async/<! (qa/fetch bucket credentials input-queries))]
      {:found (when-not (empty? found) found)
       :error (when-not (empty? errors) errors)})))

(defmethod perform [:put :pipeline-job] [{:keys [code-pipeline]} action]
  (ac/perform code-pipeline action))
