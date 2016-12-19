(ns app.mappings
  (:require [backend-shared.service.index :refer [perform fetch]]
            [shared.protocols.actionable :as ac]
            [cljs.core.async :as async]
            [shared.protocols.queryable :as qa]
            [cljs.nodejs :as node]
            [shared.protocols.loggable :as log])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def Unzip (node/require "jszip"))
(def toArray (node/require "stream-to-array"))

(defmethod fetch :artifacts [{:keys [bucket]} {:keys [input-queries credentials] :as query}]
  (go
    (let [{:keys [found errors]} (async/<! (qa/fetch bucket credentials input-queries))]
      {:found (when-not (empty? found) found)
       :error (when-not (empty? errors) errors)})))

(defmethod perform [:put :pipeline-job] [{:keys [code-pipeline]} action]
  (ac/perform code-pipeline action))

(defmethod perform [:decode :errors] [{:keys [code-pipeline]} [_ payload]]
  {:error payload})

(defmethod perform [:decode :zipfile] [{:keys [code-pipeline]} [_ payload]]
  (let [c (async/chan)]
    (log/log "x" (clj->js (keys (:files (first payload)))))
    c))
