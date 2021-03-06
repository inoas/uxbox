;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2016 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2016 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.main.ui.workspace.sidebar.options.page
  "Page options menu entries."
  (:require
   [cuerdas.core :as str]
   [rumext.alpha :as mf]
   [lentes.core :as l]
   [uxbox.common.data :as d]
   [uxbox.builtins.icons :as i]
   [uxbox.main.constants :as c]
   [uxbox.main.data.workspace :as udw]
   [uxbox.main.refs :as refs]
   [uxbox.main.store :as st]
   [uxbox.main.ui.modal :as modal]
   [uxbox.main.ui.workspace.colorpicker :refer [colorpicker-modal]]
   [uxbox.util.data :refer [parse-int]]
   [uxbox.util.dom :as dom]
   [uxbox.util.i18n :refer [tr]]))

;; (mf/defc metadata-options
;;   [{:keys [page] :as props}]
;;   (let [metadata (:metadata page)
;;         change-color
;;         (fn [color]
;;           #_(st/emit! (->> (assoc metadata :background color)
;;                            (udp/update-metadata (:id page)))))
;;         on-color-change
;;         (fn [event]
;;           (let [value (dom/event->value event)]
;;             (change-color value)))

;;         show-color-picker
;;         (fn [event]
;;           (let [x (.-clientX event)
;;                 y (.-clientY event)
;;                 props {:x x :y y
;;                        :default "#ffffff"
;;                        :value (:background metadata)
;;                        :transparent? true
;;                        :on-change change-color}]
;;             (modal/show! colorpicker-modal props)))]

;;     [:div.element-set
;;      [:div.element-set-title (tr "workspace.options.page-measures")]
;;      [:div.element-set-content
;;       [:span (tr "workspace.options.background-color")]
;;       [:div.row-flex.color-data
;;        [:span.color-th
;;         {:style {:background-color (:background metadata "#ffffff")}
;;          :on-click show-color-picker}]
;;        [:div.color-info
;;         [:input
;;          {:on-change on-color-change
;;           :value (:background metadata "#ffffff")}]]]]]))

(def default-options
  "Default data for page metadata."
  {:grid-x 10
   :grid-y 10
   :grid-color "#cccccc"})

(def options-iref
  (-> (l/key :options)
      (l/derive refs/workspace-data)))

(mf/defc grid-options
  {:wrap [mf/memo]}
  [props]
  (let [options (->> (mf/deref options-iref)
                     (merge default-options))
        on-x-change
        (fn [event]
          (let [value (-> (dom/get-target event)
                          (dom/get-value)
                          (d/parse-integer 0))]
            (st/emit! (udw/update-options {:grid-x value}))))

        on-y-change
        (fn [event]
          (let [value (-> (dom/get-target event)
                          (dom/get-value)
                          (d/parse-integer 0))]
            (st/emit! (udw/update-options {:grid-y value}))))

        change-color
        (fn [color]
          (st/emit! (udw/update-options {:grid-color color})))

        on-color-change
        (fn [event]
          (let [value (-> (dom/get-target event)
                          (dom/get-value))]
            (change-color value)))

        show-color-picker
        (fn [event]
          (let [x (.-clientX event)
                y (.-clientY event)
                props {:x x :y y
                       :transparent? true
                       :default "#cccccc"
                       :attr :grid-color
                       :on-change change-color}]
            (modal/show! colorpicker-modal props)))]
    [:div.element-set
     [:div.element-set-title (tr "workspace.options.grid-options")]
     [:div.element-set-content
      [:div.row-flex
       [:span.element-set-subtitle (tr "workspace.options.size")]
       [:div.input-element.pixels
        [:input.input-text {:type "number"
                            :value (:grid-x options)
                            :on-change on-x-change}]]
       [:div.input-element.pixels
        [:input.input-text {:type "number"
                            :value (:grid-y options)
                            :on-change on-y-change}]]]
      [:div.row-flex.color-data
       [:span.element-set-subtitle (tr "workspace.options.color")]
       [:span.color-th {:style {:background-color (:grid-color options)}
                        :on-click show-color-picker}]
       [:div.color-info
        [:input {:on-change on-color-change
                 :value (:grid-color options)}]]]]]))

(mf/defc options
  [{:keys [page] :as props}]
  [:div
   [:& grid-options {:page page}]])

