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
(def fs (node/require "fs"))

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
        output-path {:path "/tmp/extracted/"}
        read-stream (.Reader fstream (:filename payload))
        write-stream (.Extract unzip (clj->js output-path))]
    (.on write-stream "close" #(async/put! c output-path))
    (.pipe read-stream write-stream)
    c))

(defn read-file [path]
  (let [c (async/chan)]
    (.readFile fs "extracted/buildspec.yml" #(async/put! c %2))
    c))

(defmethod perform [:put :file-path] [{:keys [bucket]} [_ payload :as action]]
  (go
    (let [key "buildspec.yml"
          payload (async/<! (read-file (str "/tmp/extracted/" key)))
          items [{:file-name key
                  :content payload}]]
      (async/<! (ac/perform bucket [:put items])))))
