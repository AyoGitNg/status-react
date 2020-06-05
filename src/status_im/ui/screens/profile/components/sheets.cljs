(ns status-im.ui.screens.profile.components.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.screens.profile.components.styles :as styles])
  (:require-macros [status-im.utils.views :as views]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(views/defview block-contact []
  (views/letsubs [{:keys [public-key]} [:bottom-sheet/options]]
    [react/view
     [react/text {:style styles/sheet-text}
      (i18n/label :t/block-contact-details)]
     [quo/list-item
      {:theme               :negative
       :accessibility-label :block-contact-confirm
       :title               (i18n/label :t/block-contact)
       :on-press            #(hide-sheet-and-dispatch [:contact.ui/block-contact-confirmed public-key])}]]))
