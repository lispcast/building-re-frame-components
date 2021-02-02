(defproject building-re-frame-components "0.1.0-SNAPSHOT"
  :description "Code to accompany Building Re-frame Components, a course on PurelyFunctional.tv"
  :url "https://purelyfunctional.tv/courses/building-re-frame-components/"
  :license {:name "CC0 1.0 Universal (CC0 1.0) Public Domain Dedication"
            :url "http://creativecommons.org/publicdomain/zero/1.0/"}

  :min-lein-version "2.7.1"

  :dependencies [[day8.re-frame/http-fx "0.2.2"]
                 [re-frame "1.1.2"]
                 [org.clojure/clojure "1.10.2"]
                 [org.clojure/clojurescript "1.10.764"]
                 [org.clojure/core.async  "1.3.610"
                  :exclusions [org.clojure/tools.reader]]
                 [com.bhauman/figwheel-main "0.2.12"]
                 [compojure "1.6.2"]]

  :source-paths ["src"]

  :aliases {"fig-dev"   ["trampoline" "run" "-m" "figwheel.main" "--" "--build" "dev" "--repl"]
            "fig-build" ["trampoline" "run" "-m" "figwheel.main" "--" "-O" "advanced" "--build-once" "dev"]}

  ;; setting up nREPL for Figwheel and ClojureScript dev
  :profiles {:dev {:dependencies [[cider/piggieback "0.5.2"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   :plugins [[cider/cider-nrepl "0.25.8"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
                   ;; need to add the compliled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
    