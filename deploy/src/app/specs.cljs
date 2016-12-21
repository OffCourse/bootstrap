(ns app.specs
  (:require [cljs.spec :as spec]
            [shared.specs.action :as action :refer [action-spec]]))

(spec/def :offcourse/query (spec/or :artifacts :aws/build-artifacts))

(spec/def ::path string?)
(spec/def ::file-path (spec/keys :req-un [::path]))
(spec/def ::file-name string?)
(spec/def ::content any?)
(spec/def ::file (spec/keys :req-un [::file-name ::content]))

(defmethod action-spec :decode [_]
  (spec/tuple :offcourse/actions (spec/or :zipfile any?)))

(spec/def :offcourse/payload (spec/or :pipeline-job :aws/code-pipeline-job
                                      :file-path ::file-path
                                      :files (spec/coll-of ::file)))

(defmethod action-spec :put [_]
  (spec/tuple :offcourse/actions :offcourse/payload))
