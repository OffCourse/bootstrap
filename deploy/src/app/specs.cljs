(ns app.specs
  (:require [cljs.spec :as spec]
            [shared.specs.action :as action :refer [action-spec]]))

(spec/def :offcourse/query (spec/or :artifacts :aws/build-artifacts))

(defmethod action-spec :decode [_]
  (spec/tuple :offcourse/actions (spec/or :zipfile any?)))

(defmethod action-spec :put [_]
  (spec/tuple :offcourse/actions (spec/or :pipeline-job :aws/code-pipeline-job)))
