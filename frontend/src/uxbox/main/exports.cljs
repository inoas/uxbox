;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.main.exports
  "The main logic for SVG export functionality."
  (:require
   [rumext.alpha :as mf]
   [uxbox.util.uuid :as uuid]
   [uxbox.util.math :as mth]
   [uxbox.main.geom :as geom]
   [uxbox.util.geom.point :as gpt]
   [uxbox.util.geom.matrix :as gmt]
   [uxbox.main.ui.shapes.frame :as frame]
   [uxbox.main.ui.shapes.circle :as circle]
   [uxbox.main.ui.shapes.icon :as icon]
   [uxbox.main.ui.shapes.image :as image]
   [uxbox.main.ui.shapes.path :as path]
   [uxbox.main.ui.shapes.rect :as rect]
   [uxbox.main.ui.shapes.text :as text]
   [uxbox.main.ui.shapes.group :as group]))

(mf/defc background
  []
  [:rect
   {:x 0 :y 0
    :width "100%"
    :height "100%"
    :fill "#AFB2BF"}])

(defn- calculate-dimensions
  [data]
  (let [shapes (vals (:objects data))
        shape (geom/shapes->rect-shape shapes)
        width (+ (:x shape) (:width shape) 100)
        height (+ (:y shape) (:height shape) 100)]
    {:width (if (mth/nan? width) 100 width)
     :height (if (mth/nan? height) 100 height)}))

(declare frame-shape)
(declare group-shape)

(mf/defc frame-wrapper
  [{:keys [shape objects] :as props}]
  (let [childs (mapv #(get objects %) (:shapes shape))]
    [:& frame-shape {:shape shape :childs childs}]))

(mf/defc group-wrapper
  [{:keys [shape-wrapper shape objects] :as props}]
  (let [children (mapv #(get objects %) (:shapes shape))]
    [:& group-shape {:shape shape :children children}]))

(mf/defc shape-wrapper
  [{:keys [frame shape objects] :as props}]
  (when (and shape (not (:hidden shape)))
    (let [shape (geom/transform-shape frame shape)]
      (case (:type shape)
        :curve [:& path/path-shape {:shape shape}]
        :text [:& text/text-shape {:shape shape}]
        :icon [:& icon/icon-shape {:shape shape}]
        :rect [:& rect/rect-shape {:shape shape}]
        :path [:& path/path-shape {:shape shape}]
        :image [:& image/image-shape {:shape shape}]
        :circle [:& circle/circle-shape {:shape shape}]
        :group [:& group-wrapper {:shape shape :objects objects}]
        nil))))

(def group-shape (group/group-shape shape-wrapper))
(def frame-shape (frame/frame-shape shape-wrapper))

(mf/defc page-svg
  [{:keys [data] :as props}]
  (let [objects (:objects data)
        root    (get objects uuid/zero)
        shapes  (->> (:shapes root)
                     (map #(get objects %)))
        dim (calculate-dimensions data)]
    [:svg {:view-box (str "0 0 " (:width dim 0) " " (:height dim 0))
           :version "1.1"
           :xmlnsXlink "http://www.w3.org/1999/xlink"
           :xmlns "http://www.w3.org/2000/svg"}
     [:& background]
     (for [item shapes]
       (if (= (:type item) :frame)
         [:& frame-wrapper {:shape item
                            :key (:id item)
                            :objects objects}]
         [:& shape-wrapper {:shape item
                            :key (:id item)
                            :objects objects}]))]))

