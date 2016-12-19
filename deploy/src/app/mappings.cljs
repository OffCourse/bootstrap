(ns app.mappings
  (:require [backend-shared.service.index :refer [perform fetch]]
            [shared.protocols.actionable :as ac]
            [cljs.core.async :as async]
            [shared.protocols.queryable :as qa]
            [cljs.nodejs :as node]
            [shared.protocols.loggable :as log]
            [shared.protocols.convertible :as cv])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def Unzip (node/require "jszip"))
(def fs (node/require "fs"))

(defmethod fetch :artifacts [{:keys [bucket]} {:keys [input-queries credentials] :as query}]
  (go
    (let [{:keys [found errors]} (async/<! (qa/fetch bucket credentials input-queries))]
      {:found (when-not (empty? found) found)
       :error (when-not (empty? errors) errors)})))

(defmethod perform [:put :pipeline-job] [{:keys [code-pipeline]} action]
  (ac/perform code-pipeline action))

(defmethod perform [:decode :errors] [{:keys [code-pipeline]} [_ payload]]
  {:error payload})

(defmethod perform [:decode :zipfile] [{:keys [code-pipeline]} [_ payload :as action]]
  (let [c (async/chan)]
    (let [data (.readFileSync fs (:filename (first payload)))]
      (.then (.loadAsync Unzip data) #(async/put! c (keys (first (vals (:files (cv/to-clj %1))))))))
    c))
