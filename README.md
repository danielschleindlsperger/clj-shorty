# Shorty

A simple URL shortening service.

## Getting started

```sh
# migrate database
./sqitch deploy db:pg://root:root@localhost:5432/shorty
```

```sh
# start the clojure repl in the `user` namespace

(dev) # switch to dev namespace

(go) # start the machinery

# if changes are not reflected after evaluation, you can hit `cider-ns-refresh` (CIDER only obviously)
```

## TODO

- Move all http logic so single handler file, since it's not so much stuff
- Move all hiccup stuff to separate template namespace
- Check if we can move the squitch folders and files to a subdirectory
- Build step for tailwind
- Ring middleware
  - Security
- POST validation
  - is url
  - check if url behind url exists?
- Lint with kondo

## Features

- Remember old URLs with Session?
