(ns ring_web.core
  (:require	[clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-dns.core :as dns]
            [hiccup.core :as hiccup]))

(defn mira-ip [ip]
  (let [ret (try
              (dns/reverse-dns-lookup ip)
              (catch java.net.UnknownHostException _ ::non-encontrada))]
    (println "mira-ip" ip "->" ret)
    ret))

(defn sshlog []
  (hiccup/html
   [:html {}
    [:head
     [:title "sshlog"]
     [:style "table, th, td {border: 1px solid black; border-collapse: collapse; padding: 0.3em;}"]]
    [:body
     [:h2 "SSH connnection attempts"]
     (into
      [:table {:cellspacing 40}
       [:tr [:td "Date"] [:td "User"] [:td "IP"] [:td "hostname"]]]
      ;; csv/read-csv devolve umha sequência de vectores:
      ;; Se o arquivo tém:
      ;;
      ;;    um,dous,três,quatro
      ;;    bla,ble,bli,blo
      ;;
      ;; devolve
      ;;
      ;;    (["um" "dous" "três" "quatro"]
      ;;     ["bla" "ble" "bli" "blo"])
      ;;
      ;; Tamém tém em conta comentários (que em csv começam por ").
      (for [line (csv/read-csv (io/reader "static/sshlog.log"))]
        [:tr (for [entry line]
               [:td entry])]))]]))

(defn sshlog2 []
  (str
   "<head>
    <title>sshlog</title>
    <style>table, th, td {border: 1px solid black;border-collapse: collapse; padding: 0.3em;}</style>
  </head>"
   "<body>"
   "<h2>Attempts ssh connections</h2>"
   "<table cellspacing=40>"
   "<tr><td>Date</td><td>User</td><td>IP</td><td>hostname</td></tr>"
   (apply str
          (map #(str "\n<tr><td>" (str/replace % #"," "<td>") "</tr>")
               (str/split (slurp "static/sshlog.log") #"\n")))
   "\n</table>"
   "</body>"
   ))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body 
		(cond
			(= (get request :uri) "/sshlog")
					(sshlog)
			(= (get request :uri) "/request") 
					(hiccup/html 
						[:html {}
							[:head {} [:title {} "request"]]
							[:body {} [:pre {} (with-out-str (pp/pprint request))]]
						]
					)
			:else 
					(slurp "static/index.html")
		)
   })