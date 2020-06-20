# Shorty

A simple URL shortening service.

## Getting started

```sh
# migrate database
./sqitch deploy db:pg://root:root@localhost:5432/shorty

# revert migration
./sqitch revert db:pg://root:root@localhost:5432/shorty
```

```sh
# start the clojure repl in the `user` namespace

(dev) # switch to dev namespace

(go) # start the machinery

# if changes are not reflected after evaluation, you can hit `cider-ns-refresh` (CIDER only obviously)
```

### Linting

[Install clj-kondo](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md) to be available in $PATH.

```sh
clj-kondo --lint "$(clojure -Spath)"
```

*Hint* clj-kondo reports `unresolved symbol` when using `mount`'s `defstate` macro. Circumvent with the usage of `declare`:
```clj
(declare db)
(defstate db
  :start {})
```

## TODO

- Move all http logic so single handler file, since it's not so much stuff
- Move all hiccup stuff to separate template namespace
- Build step for tailwind
- Ring middleware
- Security
- POST validation
  - is url
  - check if url behind url exists?

- CI
- Deployment

## Features

- Remember old URLs with Session?
