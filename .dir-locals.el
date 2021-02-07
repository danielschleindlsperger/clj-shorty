;;; Directory Local Variables
;;; For more information see (info "(emacs) Directory Variables")

((clojure-mode . ((cider-clojure-cli-global-options . "-A:dev")
                  (cider-ns-refresh-before-fn . "integrant.repl/halt")
                  (cider-ns-refresh-after-fn . "integrant.repl/go"))))
