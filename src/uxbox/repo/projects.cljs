;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.repo.projects
  "A main interface for access to remote resources."
  (:require [beicon.core :as rx]
            [uxbox.repo.core :refer (-do url send!)]
            [uxbox.state :as ust]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Login
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod -do :fetch/projects
  [type data]
  (let [url (str url "/projects")]
    (send! {:url url :method :get})))

(defmethod -do :fetch/pages
  [type data]
  (send! {:url (str url "/pages") :method :get}))

(defmethod -do :fetch/pages-by-project
  [type {:keys [project] :as params}]
  (let [url (str url "/projects/" project "/pages")]
    (send! {:method :get :url url})))

(defmethod -do :create/project
  [_ data]
  (let [params {:url (str url "/projects")
                :method :post
                :body data}]
    (send! params)))

(defmethod -do :delete/project
  [_ id]
  (let [url (str url "/projects/" id)]
    (send! {:url url :method :delete})))

(defmethod -do :delete/page
  [_ id]
  (let [url (str url "/pages/" id)]
    (send! {:url url :method :delete})))

(defmethod -do :create/page
  [type {:keys [id] :as data}]
  (let [params {:url (str url "/pages")
                :method :post
                :body data}]
    (send! params)))

(defmethod -do :update/page
  [type {:keys [id] :as data}]
  (let [params {:url (str url "/pages/" id)
                :method :put
                :body data}]
    (send! params)))
