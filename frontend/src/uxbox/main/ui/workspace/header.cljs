;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2015-2017 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2017 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.main.ui.workspace.header
  (:require
   [lentes.core :as l]
   [rumext.alpha :as mf]
   [uxbox.builtins.icons :as i :include-macros true]
   [uxbox.config :as cfg]
   [uxbox.main.data.history :as udh]
   [uxbox.main.data.workspace :as dw]
   [uxbox.main.refs :as refs]
   [uxbox.main.store :as st]
   [uxbox.main.ui.modal :as modal]
   [uxbox.main.ui.workspace.images :refer [import-image-modal]]
   [uxbox.main.ui.components.dropdown :refer [dropdown]]
   [uxbox.util.i18n :as i18n :refer [tr t]]
   [uxbox.util.data :refer [classnames]]
   [uxbox.util.math :as mth]
   [uxbox.util.router :as rt]))

;; --- Zoom Widget

(mf/defc zoom-widget
  {:wrap [mf/memo]}
  [props]
  (let [zoom (mf/deref refs/selected-zoom)
        show-dropdown? (mf/use-state false)
        increase #(st/emit! dw/increase-zoom)
        decrease #(st/emit! dw/decrease-zoom)
        zoom-to-50 #(st/emit! dw/zoom-to-50)
        zoom-to-100 #(st/emit! dw/reset-zoom)
        zoom-to-200 #(st/emit! dw/zoom-to-200)]
    [:div.zoom-input
     [:span.add-zoom {:on-click decrease} "-"]
     [:div {:on-click #(reset! show-dropdown? true)}
      [:span {} (str (mth/round (* 100 zoom)) "%")]
      [:span.dropdown-button i/arrow-down]
      [:& dropdown {:show @show-dropdown?
                    :on-close #(reset! show-dropdown? false)}
       [:ul.zoom-dropdown
        [:li {:on-click increase}
         "Zoom in" [:span "+"]]
        [:li {:on-click decrease}
         "Zoom out" [:span "-"]]
        [:li {:on-click zoom-to-50}
         "Zoom to 50%" [:span "Shift + 0"]]
        [:li {:on-click zoom-to-100}
         "Zoom to 100%" [:span "Shift + 1"]]
        [:li {:on-click zoom-to-200}
         "Zoom to 200%" [:span "Shift + 2"]]]]]
     [:span.remove-zoom {:on-click increase} "+"]]))

;; --- Header Users

(mf/defc user-widget
  [{:keys [user self?] :as props}]
  (let [photo (or (:photo-uri user)
                  (if self?
                    "/images/avatar.jpg"
                    "/images/avatar-red.jpg"))]
    [:li.tooltip.tooltip-bottom
     {:alt (:fullname user)
      :on-click (when self?
                  #(st/emit! (rt/navigate :settings/profile)))}
     [:img {:style {:border-color (:color user)}
            :src photo}]]))

(mf/defc active-users
  [props]
  (let [profile (mf/deref refs/profile)
        users (mf/deref refs/workspace-users)]
    [:ul.user-multi
     [:& user-widget {:user profile :self? true}]
     (for [id (->> (:active users)
                   (remove #(= % (:id profile))))]
       [:& user-widget {:user (get-in users [:by-id id])
                        :key id}])]))

(mf/defc menu
  [{:keys [layout] :as props}]
  (let [show-menu? (mf/use-state false)
        locale (i18n/use-locale)]

    [:*
     [:div.menu-btn {:on-click #(reset! show-menu? true)} i/actions]

     [:& dropdown {:show @show-menu?
                   :on-close #(reset! show-menu? false)}
      [:ul.workspace-menu
       [:li {:on-click #(st/emit! (dw/toggle-layout-flag :rules))}
        [:span i/ruler]
        [:span
         (if (contains? layout :rules)
           (t locale "workspace.header.menu.hide-rules")
           (t locale "workspace.header.menu.show-rules"))]]

       [:li {:on-click #(st/emit! (dw/toggle-layout-flag :grid))}
        [:span i/grid]
        [:span
         (if (contains? layout :grid)
           (t locale "workspace.header.menu.hide-grid")
           (t locale "workspace.header.menu.show-grid"))]]

       [:li {:on-click #(st/emit! (dw/toggle-layout-flag :layers))}
        [:span i/layers]
        [:span
         (if (contains? layout :layers)
           (t locale "workspace.header.menu.hide-layers")
           (t locale "workspace.header.menu.show-layers"))]]

       [:li {:on-click #(st/emit! (dw/toggle-layout-flag :colorpalette))}
        [:span i/palette]
        [:span
         (if (contains? layout :colorpalette)
           (t locale "workspace.header.menu.hide-palette")
           (t locale "workspace.header.menu.show-palette"))]]

       [:li {:on-click #(st/emit! (dw/toggle-layout-flag :libraries))}
        [:span i/icon-set]
        [:span
         (if (contains? layout :libraries)
           (t locale "workspace.header.menu.hide-libraries")
           (t locale "workspace.header.menu.show-libraries"))]]
       ]]]))

;; --- Header Component

(def router-ref
  (-> (l/key :router)
      (l/derive st/state)))

(mf/defc header
  [{:keys [page file layout project] :as props}]
  (let [go-to-dashboard #(st/emit! (rt/nav :dashboard-team {:team-id "self"}))
        toggle-sitemap #(st/emit! (dw/toggle-layout-flag :sitemap))
        locale (i18n/use-locale)
        router (mf/deref router-ref)
        view-url (rt/resolve router :viewer {:page-id (:id page)} {:index 0})]
    [:header.workspace-bar
     [:div.main-icon
      [:a {:on-click go-to-dashboard} i/logo-icon]]

     [:& menu {:layout layout}]

     [:div.project-tree-btn {:alt (tr "header.sitemap")
                             :class (classnames :selected (contains? layout :sitemap))
                             :on-click toggle-sitemap}
      [:span.project-name (:name project) " /"]
      [:span (:name file)]]

     [:div.workspace-options
      [:& active-users]]

     [:& zoom-widget]

     [:a.preview {
                  ;; :target "__blank"
                  :href (str "#" view-url)} i/play]]))
