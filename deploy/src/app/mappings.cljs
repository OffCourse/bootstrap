(ns app.mappings
  (:require [backend-shared.service.index :refer [perform fetch]]
            [shared.protocols.actionable :as ac]
            [cljs.core.async :as async]
            [shared.protocols.queryable :as qa]
            [cljs.nodejs :as node]
            [shared.protocols.loggable :as log]
            [shared.protocols.convertible :as cv])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def fstream (node/require "fstream"))
(def unzip (node/require "unzipper"))

(defmethod fetch :artifacts [{:keys [bucket]} {:keys [input-queries credentials] :as query}]
  (go
    (let [{:keys [found errors]} (async/<! (qa/fetch bucket credentials input-queries))]
      {:found (when-not (empty? found) (first found))
       :error (when-not (empty? errors) errors)})))

(defmethod perform [:put :pipeline-job] [{:keys [code-pipeline]} action]
  (ac/perform code-pipeline action))

(defmethod perform [:decode :errors] [{:keys [code-pipeline]} [_ payload]]
  {:error payload})

(defmethod perform [:decode :zipfile] [{:keys [code-pipeline]} [_ payload :as action]]
  (let [c (async/chan)
        output-path {:path "extracted/"}
        read-stream (.Reader fstream (:filename payload))
        write-stream (.Extract unzip (clj->js output-path))]
    (.on write-stream "close" #(async/put! c output-path))
    (.pipe read-stream write-stream)
    c))
