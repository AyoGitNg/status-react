(ns status-im.hardwallet.wallet
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.hardwallet.common :as common]
            [status-im.constants :as constants]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.utils.hex :as utils.hex]))

(fx/defn show-pin-sheet
  {:events [:hardwallet/new-account-pin-sheet]}
  [{:keys [db] :as cofx}]
  (fx/merge
   cofx
   {:db (-> db
            (assoc-in [:hardwallet :pin :enter-step] :export-key)
            (update-in [:hardwallet :pin] dissoc :export-key)
            (assoc :hardwallet/new-account-sheet? true))}))

(fx/defn verify-pin-with-delay
  [cofx]
  {:utils/dispatch-later
   ;; We need to give previous sheet some time to be fully hidden 
   [{:ms 200
     :dispatch [:wallet.accounts/verify-pin]}]})

(fx/defn hide-pin-sheet
  {:events [:hardwallet/new-account-pin-sheet-hide]}
  [{:keys [db]}]
  {:db (assoc db :hardwallet/new-account-sheet? false)})

(fx/defn generate-new-keycard-account
  {:events [:wallet.accounts/generate-new-keycard-account]}
  [{:keys [db]}]
  (let [path-num (inc (get-in db [:multiaccount :latest-derived-path]))
        path     (str constants/path-wallet-root "/" path-num)
        pin      (common/vector->string (get-in db [:hardwallet :pin :export-key]))
        pairing  (common/get-pairing db)]
    {:db
     (assoc-in
      db [:hardwallet :on-export-success]
      #(vector :wallet.accounts/account-stored
               (let [public-key (utils.hex/normalize-hex %)]
                 {;; Strip leading 04 prefix denoting uncompressed key format
                  :address    (eip55/address->checksum
                               (str "0x"
                                    (ethereum/public-key->address
                                     (subs public-key 2))))
                  :public-key (str "0x" public-key)
                  :path       path})))

     :hardwallet/export-key {:pin pin :pairing pairing :path path}}))

(fx/defn verify-pin
  {:events [:wallet.accounts/verify-pin]}
  [cofx]
  (common/verify-pin
   cofx
   {:pin-step          :export-key
    :on-card-connected :wallet.accounts/verify-pin
    :on-success        :wallet.accounts/generate-new-keycard-account
    :on-failure        :hardwallet/new-account-pin-sheet}))
